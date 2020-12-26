package com.tensor.export.dto;

import java.util.Collections;
import java.util.List;

/**
 * @author leeweit
 * @version 1.0
 * Create Time 2020/12/24 17:39
 */
public class Page<T> {

    /**
     * 总记录数
     */
    private Integer totalCount;
    /**
     * 每页多少条
     */
    private Integer pageSize;
    /**
     * 第几页
     */
    private Integer pageNum;
    /**
     * 总页数
     */
    private Integer totalPages;
    /**
     * 结果
     */
    private List items;

    private Page(Builder b) {
        this.totalCount = b.totalCount;
        this.pageSize = b.pageSize;
        this.pageNum = b.pageNum;
        this.totalPages = b.totalPages;
        this.items = b.items;
    }

    public static class Builder<T> {
        private Integer totalCount = 0;
        private Integer pageSize = 0;
        private Integer pageNum = 0;
        private Integer totalPages = 0;
        private List<T> items = Collections.emptyList();

        private Builder() {
        }

        public Page build() {
            return new Page(this);
        }

        public static Builder init() {
            return new Builder();
        }

        public Builder totalCount(Integer totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder pageNum(Integer pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public Builder totalPages(Integer totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder items(List<T> items) {
            this.items = items;
            return this;
        }

    }


    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Page{" +
                "totalCount=" + totalCount +
                ", pageSize=" + pageSize +
                ", pageNum=" + pageNum +
                ", totalPages=" + totalPages +
                ", items=" + items +
                '}';
    }
}
