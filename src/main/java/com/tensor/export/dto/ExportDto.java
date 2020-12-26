package com.tensor.export.dto;


import com.tensor.export.api.ExportFiled;

/**
 * @author wei.li
 * @version 1.0
 * Create Time 2020/12/03 16:21
 */
public class ExportDto {

    @ExportFiled(index = 1, title = "ID")
    private String id;

    @ExportFiled(index = 2, title = "名称")
    private String name;

    public ExportDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
