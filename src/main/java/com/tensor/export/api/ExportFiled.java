package com.tensor.export.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportFiled {

    /**
     * 导出excel列索引
     *
     * @return int
     */
    int index();

    /**
     * 导出excel列名
     *
     * @return String
     */
    String title();

}
