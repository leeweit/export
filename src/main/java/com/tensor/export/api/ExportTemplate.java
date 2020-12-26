package com.tensor.export.api;

import com.alibaba.fastjson.JSON;
import com.tensor.export.dto.ExportFileDto;
import com.tensor.export.dto.ExportFileHistory;
import com.tensor.export.dto.Page;
import com.tensor.export.dto.Result;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wei.li
 * @version 1.0
 * Create Time 2020/7/1 10:21
 */
public class ExportTemplate<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExportTemplate.class);

    private Class clz;

    public ExportTemplate(Class clz) {
        this.clz = clz;
    }

    public interface CallbackHandler {

        Result<Integer> getTotalSize();

        <T> Result<Page<T>> dataset(int pageNum, int pageSize);

        Result export(ByteArrayOutputStream out) throws Exception;
    }

    public interface SaveHistoryHandler {

        Result<ExportFileHistory> save(String fileUrl);

        Result<List<String>> getHistoryList();

    }

    public static abstract class AbstractHandler implements CallbackHandler {

        private String fileName;

        public AbstractHandler() {
        }

        public AbstractHandler(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public Result<Integer> getTotalSize() {
            return null;
        }

        @Override
        public <T> Result<Page<T>> dataset(int pageNum, int pageSize) {
            return null;
        }

        @Override
        public Result export(ByteArrayOutputStream out) throws Exception {
            String workPath = getWorkPath(DateFormatUtils.format(new Date(), "yyyyMMdd"));
            File dir = new File(workPath);
            if (dir.exists() || (!dir.exists() && dir.mkdir())) {
                String file = workPath + "/" + fileName;
                try (FileOutputStream fileOutputStream = new FileOutputStream(new File(file))) {
                    out.writeTo(fileOutputStream);
                }
                return Result.success(file);
            }
            return Result.fail(1, "make root dir fail");
        }

    }

    public static abstract class AbstractAsyncHandler extends AbstractHandler implements SaveHistoryHandler {

        public AbstractAsyncHandler() {
        }

        public AbstractAsyncHandler(String fileName) {
            super(fileName);
        }

        @Override
        public Result<ExportFileHistory> save(String fileUrl) {
            return null;
        }

        @Override
        public Result<List<String>> getHistoryList() {
            return Result.success(getConfigs());
        }

    }

    public Result<List<String>> getHistoryList(AbstractAsyncHandler handler) {
        return handler.getHistoryList();
    }

    public Result syncExport(AbstractHandler handler) {
        return execute(handler);
    }

    public Result asyncExport(ThreadsExecutorService executorService, AbstractAsyncHandler handler) {
        executorService.execute(() -> {
            Result result = execute(handler);
            Result<ExportFileHistory> historyResult;
            if (result.success()) {
                historyResult = handler.save(result.getBody().toString());
            } else {
                historyResult = handler.save(null);
                ExportFileHistory exportFileHistory = historyResult.getBody();
                exportFileHistory.getExportFileDto().setStatus(result.getMsg());
            }
            int size = 3;
            String json = JSON.toJSONString(historyResult.getBody().getExportFileDto());
            List<String> temps = getConfigs();
            List<String> temps0 ;
            temps.add(0, json);
            if (temps.size() > size) {
                temps0 = temps.subList(0, size);
                for (int i = size ; i < temps.size(); i++) {
                    ExportFileDto s = JSON.parseObject(temps.get(i), ExportFileDto.class);
                    clearDirOrFiles(s.getFileUrl());
                }
            }else{
                temps0 = temps;
            }
            String string = String.join("\n", temps0);
            try (FileOutputStream fileOutputStream = new FileOutputStream(getConfigFile())) {
                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
                    bufferedOutputStream.write(string.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    // ignore
                }
            } catch (IOException e) {
                // ignore
            }

        });

        return Result.fail(0, "请稍候在列表中查看导出文件");
    }

    private Result execute(CallbackHandler handler) {
        long start = System.currentTimeMillis();
        Result<Integer> totalSize = handler.getTotalSize();
        if (!totalSize.success()) {
            return totalSize;
        }
        // 刷新工作簿的数量与查询的每页的数量,两者设置成一样,每查询一页的数据都刷新到文件,降低内存占用
        int rowAccessWindowSize = 200;
        SXSSFWorkbook workbook = new SXSSFWorkbook(rowAccessWindowSize);
        workbook.setCompressTempFiles(true);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // sheet标题索引起始位
            int s = 1;
            // sheet标题索引结束位
            int e;
            int a;
            // 数据集的分页的总页数
            int totalPages;
            // 行索引
            int index;
            // 查询的页码
            int pageNum = 1;
            SXSSFSheet sheet;
            Result<Page<T>> result;
            Page<T> page;
            // 刷新excel,刷新内存的数据量,降低数据导出过程中内存的占用
            // excel每个sheet的数量,刷新内存数据,降低内存占用,必须大于等于pageSize,且excelSheetAmount % pageSize =0
            int excelSheetAmount = rowAccessWindowSize * 50;
            int total = totalSize.getBody();
            int sheetNo = total % excelSheetAmount == 0 ? total / excelSheetAmount : total / excelSheetAmount + 1;
            XSSFCellStyle style = createStyle(workbook, true);
            XSSFCellStyle style2 = createStyle(workbook, false);
            List<TempField> fields = initField(this.clz);
            for (int b = 1; b <= sheetNo; b++) {
                int flush = 0;
                a = b * excelSheetAmount;
                e = a > total ? total : a;
                sheet = createSheet(workbook, s + "-" + e);
                createTitleRow(sheet, style, fields);
                result = handler.dataset(pageNum, rowAccessWindowSize);
                page = result.getBody();
                totalPages = page.getTotalPages();
                while (++pageNum <= totalPages + 1) {
                    index = flush;
                    dataSet(workbook, sheet, style2, page.getItems(), fields, index);
                    flush += page.getItems().size();
                    // sheet的行数达到阀值跳出查询,重新创建一个sheet.
                    if (flush >= excelSheetAmount) {
                        break;
                    }
                    result = handler.dataset(pageNum, rowAccessWindowSize);
                    page = result.getBody();
                }
                s = a + 1;
            }
            workbook.write(out);
            return handler.export(out);
        } catch (Exception e) {
            logger.error("导出数据出错:", e);
        } finally {
            workbook.dispose();
            try {
                workbook.close();
            } catch (IOException e) {
                // ignore
            }
            logger.info("导出数据共耗时:{}", System.currentTimeMillis() - start);
        }
        return Result.fail(400, "导出数据失败");
    }


    private void createTitleRow(Sheet sheet, CellStyle style, List<TempField> fields) {
        // 表格标题行
        Row row = sheet.createRow(0);
        int i = 0;
        for (TempField m : fields) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
            cell.setCellValue(new XSSFRichTextString(m.title));
            i++;
        }
    }

    private void dataSet(Workbook workbook, Sheet sheet, CellStyle style2,
                         Collection<T> dataset, List<TempField> fields, int index) {
        Drawing<? extends Shape> patriarch = sheet.createDrawingPatriarch();
        Row row;
        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        Font font3 = workbook.createFont();
        Pattern p = Pattern.compile("^//d+(//.//d+)?$");
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            T t = it.next();
            int i = 0;
            for (TempField m : fields) {
                String fieldName = m.column;
                String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Class<?> tCls = t.getClass();
                Class<?> clz = tCls;
                for (; clz != Object.class; clz = tCls.getSuperclass()) {
                    try {
                        Method getMethod = clz.getMethod(getMethodName);
                        Object value = getMethod.invoke(t);
                        createCell(workbook, sheet, style2, font3, p, patriarch, row, i, value);
                    } catch (Exception e) {
                        /// 不处理
                    }
                    tCls = clz;
                }
                i++;
            }
        }
    }

    private List<TempField> initField(Class<?> clz) {
        ExportFiled exportFiled;
        List<TempField> tempFields = new ArrayList<>();
        Class<?> clazz = clz;
        // 把父类的所有的属性取出来
        for (; clazz != Object.class; clazz = clz.getSuperclass()) {
            try {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getType() != clazz.getDeclaringClass()) {
                        exportFiled = field.getAnnotation(ExportFiled.class);
                        if (exportFiled != null) {
                            tempFields.add(new TempField(exportFiled.index(), field.getName(), exportFiled.title()));
                        }
                    }
                }
            } catch (Exception e) {
                // 这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
            }
            clz = clazz;
        }
        tempFields.sort((o1, o2) -> {
            if (o1.index > o2.index) {
                return 1;
            } else if (o1.index < o2.index) {
                return -1;
            }
            return 0;
        });
        return tempFields;
    }

    private class TempField {

        private int index;
        private String column;
        private String title;

        TempField(int index, String column, String title) {
            this.index = index;
            this.column = column;
            this.title = title;
        }
    }

    private SXSSFSheet createSheet(Workbook workbook, String title) {
        // 生成一个表格
        SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth((short) 16);
        return sheet;
    }

    private XSSFCellStyle createStyle(Workbook workbook, boolean boldWeight) {
        // 生成一个样式
        return style(workbook, boldWeight, HSSFColor.HSSFColorPredefined.BLACK.getIndex());
    }

    private XSSFCellStyle style(Workbook workbook, boolean boldWeight, short fontColor) {
        // 生成一个样式
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        // 设置这些样式
        style.setFillForegroundColor(HSSFColor.HSSFColorPredefined.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);//HSSFCellStyle.ALIGN_CENTER
        // 生成一个字体
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setColor(fontColor);//HSSFColor.BLACK.index
        font.setFontHeightInPoints((short) 12);
        font.setBold(boldWeight);
        // 把字体应用到当前的样式
        style.setFont(font);
        return style;
    }

    private void createCell(Workbook workbook, Sheet sheet, CellStyle style, Font font3, Pattern p, Drawing patriarch,
                            Row row, int columnIndex, Object cellValue) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellStyle(style);
        // 判断值的类型后进行强制类型转换
        String textValue = null;
        if (cellValue instanceof Date) {
            Date date = (Date) cellValue;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            textValue = sdf.format(date);
        } else if (cellValue instanceof byte[]) {
            // 有图片时，设置行高为60px;
            row.setHeightInPoints(60);
            // 设置图片所在列宽度为80px,注意这里单位的一个换算
            sheet.setColumnWidth(columnIndex, (short) (35.7 * 80));
            byte[] bsValue = (byte[]) cellValue;
            ClientAnchor anchor = new HSSFClientAnchor(0, 0, 1023, 255, (short) 6, columnIndex, (short) 6, columnIndex);
            //anchor.setAnchorType(2);
            patriarch.createPicture(anchor, workbook.addPicture(bsValue, HSSFWorkbook.PICTURE_TYPE_JPEG));
            if (workbook instanceof XSSFWorkbook) {
                anchor = new XSSFClientAnchor(0, 0, 1023, 255, (short) 6, columnIndex, (short) 6, columnIndex);
                patriarch.createPicture(anchor, workbook.addPicture(bsValue, XSSFWorkbook.PICTURE_TYPE_JPEG));
            }
        } else {
            // 其它数据类型都当作字符串简单处理
            if (null != cellValue) textValue = cellValue.toString();
        }
        // 如果不是图片数据，就利用正则表达式判断textValue是否全部由数字组成
        if (textValue != null) {
            Matcher matcher = p.matcher(textValue);
            if (matcher.matches()) {
                // 是数字当作double处理
                cell.setCellValue(Double.parseDouble(textValue));
            } else {
                RichTextString richString = new HSSFRichTextString(textValue);
                font3.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
                if (workbook instanceof SXSSFWorkbook) {
                    richString = new XSSFRichTextString(textValue);
                    font3.setColor(new XSSFColor().getIndexed());
                }
                richString.applyFont(font3);
                cell.setCellValue(richString);
            }
        }
    }

    private static void clearDirOrFiles(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    try {
                        Files.deleteIfExists(f.toPath());
                    } catch (IOException e) {
                        logger.debug("ignore{}", 2);
                    }
                }
            }
        } else if (file.isFile()) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                logger.debug("ignore{}", 0);
            }
        }
    }

    private static List<String> getConfigs() {
        File dir = getConfigFile();
        List<String> temps = new ArrayList<>();
        if (dir.exists()) {
            String temp;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(dir),
                    Charset.forName("utf-8")))) {
                while ((temp = in.readLine()) != null) {
                    temps.add(temp);
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return temps;
    }

    private static File getConfigFile() {
        String config = "config.json";
        String workPath = getBasePath() + config;
        return new File(workPath);
    }

    private static String getWorkPath(String path) {
        String exportPath = getBasePath() + path;
        File exportFile = new File(exportPath);
        if (!exportFile.exists() && exportFile.mkdirs()) {
            logger.debug("ignore{}", 3);
        }
        return exportPath + "/";
    }

    private static String getBasePath() {
        String temp = System.getProperty("user.dir") + "/temp-export/history/";
        File file = new File(temp);
        if (!file.exists() && file.mkdirs()) {
            logger.debug("ignore{}", 4);
        }
        return temp;
    }

}
