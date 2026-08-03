package com.shop.vo;

public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public Result() {}

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ========== 兼容原来的写法 ==========
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(400, msg, null);
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    // ========== 兼容我给的代码里的写法 ==========
    public static <T> Result<T> success(T data) {
        return ok(data);
    }

    public static <T> Result<T> success() {
        return ok();
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return fail(code, msg);
    }

    // ========== getter / setter ==========
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}