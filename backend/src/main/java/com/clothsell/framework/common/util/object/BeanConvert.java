package com.clothsell.framework.common.util.object;

import com.clothsell.framework.common.pojo.PageResult;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public final class BeanConvert {
    private BeanConvert() {
    }

    public static <T> T toBean(Object source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetType.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType) {
        List<T> list = new ArrayList<>();
        if (source == null) {
            return list;
        }
        for (S item : source) {
            list.add(toBean(item, targetType));
        }
        return list;
    }

    public static <S, T> PageResult<T> toBean(PageResult<S> page, Class<T> targetType) {
        return new PageResult<>(toBean(page.getList(), targetType), page.getTotal());
    }
}
