package com.clothsell.framework.common.exception.util;

import com.clothsell.framework.common.exception.ErrorCode;
import com.clothsell.framework.common.exception.ServiceException;

public final class ServiceExceptionUtil {
    private ServiceExceptionUtil() {
    }

    public static ServiceException exception(ErrorCode errorCode) {
        return new ServiceException(errorCode.getCode(), errorCode.getMsg());
    }
}
