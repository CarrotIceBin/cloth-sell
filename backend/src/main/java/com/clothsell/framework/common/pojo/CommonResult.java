package com.clothsell.framework.common.pojo;

import lombok.Data;

@Data
public class CommonResult<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = 0;
        result.msg = "";
        result.data = data;
        return result;
    }

    public static <T> CommonResult<T> error(Integer code, String msg) {
        CommonResult<T> result = new CommonResult<>();
        result.code = code;
        result.msg = msg;
        return result;
    }
}
