package com.clothsell.framework.common.util.object;

import com.clothsell.framework.common.pojo.PageResult;

import java.util.List;

public final class BeanUtils {
    private BeanUtils() {
    }

    public static <T> T toBean(Object source, Class<T> targetType) {
        return BeanConvert.toBean(source, targetType);
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType) {
        return BeanConvert.toBean(source, targetType);
    }

    public static <S, T> PageResult<T> toBean(PageResult<S> page, Class<T> targetType) {
        return BeanConvert.toBean(page, targetType);
    }
}
