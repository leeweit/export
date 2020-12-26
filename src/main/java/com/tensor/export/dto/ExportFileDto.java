package com.tensor.export.dto;

/**
 * @author wei.li
 * @version 1.0
 * Create Time 2020/12/23 17:20
 */
public class ExportFileDto {

    private int index;

    private String name;

    private String fileUrl;

    private String fileType;

    private String status;

    private String createTime;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "ExportFileDto{" +
                "index=" + index +
                ", name='" + name + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileType='" + fileType + '\'' +
                ", status='" + status + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
