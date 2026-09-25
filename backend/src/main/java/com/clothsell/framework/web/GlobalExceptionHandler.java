package com.clothsell.framework.web;

import com.clothsell.framework.common.exception.ServiceException;
import com.clothsell.framework.common.pojo.CommonResult;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public CommonResult<Object> service(ServiceException ex) {
        return CommonResult.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Object> invalid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldError() == null
                ? "参数无效" : ex.getBindingResult().getFieldError().getDefaultMessage();
        return CommonResult.error(400, msg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public CommonResult<Object> denied(AccessDeniedException ex) {
        return CommonResult.error(403, "没有权限");
    }
}
