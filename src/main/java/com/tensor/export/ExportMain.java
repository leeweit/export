package com.tensor.export;

import com.tensor.export.api.ExportTemplate;
import com.tensor.export.api.ThreadsExecutorService;
import com.tensor.export.dto.*;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wei.li
 * @version 1.0
 * Create Time 2020/12/26 18:37
 */
public class ExportMain {

    public static void main(String[] args) {
        ExportMain exportMain = new ExportMain();

        exportMain.export();
        exportMain.getHistoryList();

    }

    private void getHistoryList() {
        System.out.println("--------------->>>>" + new ExportTemplate<>(null).getHistoryList(new ExportTemplate.AbstractAsyncHandler() {
        }));
    }

    private void export() {
        String fileName = "export" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + ".xlsx";
        new ExportTemplate<>(ExportDto.class).asyncExport(ThreadsExecutorService.Singleton.INSTANCE.getInstance(),
                new ExportTemplate.AbstractAsyncHandler(fileName) {
            @Override
            public Result<Integer> getTotalSize() {
                return Result.success(10);
            }

            @Override
            public <T> Result<Page<T>> dataset(int pageNum, int pageSize) {
                List<ExportDto> data = new ArrayList<>();
                data.add(new ExportDto("1", "abc"));
                data.add(new ExportDto("2", "abc"));
                data.add(new ExportDto("3", "abc"));
                data.add(new ExportDto("4", "abc"));
                data.add(new ExportDto("5", "abc"));
                data.add(new ExportDto("6", "abc"));
                data.add(new ExportDto("7", "abc"));
                data.add(new ExportDto("8", "abc"));
                data.add(new ExportDto("9", "abc"));
                data.add(new ExportDto("10", "abc"));
                Page<T> page = Page.Builder.init()
                        .items(data)
                        .pageNum(pageNum)
                        .pageSize(pageSize)
                        .totalPages(1)
                        .totalCount(10)
                        .build();
                return Result.success(page);
            }

            @Override
            public Result<ExportFileHistory> save(String fileUrl) {
                ExportFileHistory exportFileHistory = new ExportFileHistory();
                exportFileHistory.setOperator("123");
                ExportFileDto exportFileDto = new ExportFileDto();
                exportFileDto.setName(fileName);
                exportFileDto.setFileUrl(fileUrl);
                exportFileDto.setStatus("0");
                exportFileDto.setFileType("xlsx");
                exportFileDto.setCreateTime(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                exportFileHistory.setExportFileDto(exportFileDto);
                return Result.success(exportFileHistory);
            }
        });
    }

}
