package com.tensor.export.dto;

/**
 * @author wei.li
 * @version 1.0
 * Create Time 2020/12/23 17:20
 */
public class ExportFileHistory {

    private String operator;

    private ExportFileDto exportFileDto;


    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public ExportFileDto getExportFileDto() {
        return exportFileDto;
    }

    public void setExportFileDto(ExportFileDto exportFileDto) {
        this.exportFileDto = exportFileDto;
    }

    @Override
    public String toString() {
        return "ExportFileHistory{" +
                "operator='" + operator + '\'' +
                ", exportFileDto=" + exportFileDto +
                '}';
    }
}
