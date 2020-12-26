package com.tensor.export.dto;

import java.io.Serializable;

/**
 * Created by leeweit on 2018/3/1.
 */
@SuppressWarnings({"unused"})
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T body;

    Result() {
    }

    private Result(Builder<T> builder) {
        this.code = builder.code;
        this.msg = builder.msg;
        this.body = builder.body;
    }

    public static <T> Result<T> success(T t) {
        return Builder.custom(t).code(0).msg("success").build();
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result.Builder<T>().code(code).msg(msg).build();
    }

    public static <T> Result<T> error(String msg) {
        return new Result.Builder<T>().code(400).msg("error: " + msg).build();
    }

    public boolean success() {
        return code != null && code == 0;
    }

    public static class Builder<T> {
        private Integer code;
        private String msg;
        private T body;

        public Builder(Integer code) {
            this.code = code;
        }

        private Builder() {

        }

        private Builder<T> code(Integer code) {
            this.code = code;
            return this;
        }

        private Builder<T> msg(String msg) {
            this.msg = msg;
            return this;
        }

        private Builder<T> body(T body) {
            this.body = body;
            return this;
        }

        private Result<T> build() {
            return new Result<>(this);
        }

        private static <T> Builder<T> custom(T t) {
            return new Builder<T>().body(t);
        }
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", body=" + body +
                '}';
    }

}
