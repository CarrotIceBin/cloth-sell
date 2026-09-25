package com.clothsell.framework.mybatis.core.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.time.LocalDateTime;
import java.util.Collection;

public class LambdaQueryWrapperX<T> extends LambdaQueryWrapper<T> {
    public LambdaQueryWrapperX<T> likeIfPresent(SFunction<T, ?> column, String value) {
        if (value != null && !value.isBlank()) {
            like(column, value);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> eqIfPresent(SFunction<T, ?> column, Object value) {
        if (value != null && !(value instanceof String string && string.isBlank())) {
            eq(column, value);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            in(column, values);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, LocalDateTime[] range) {
        if (range != null && range.length == 2 && range[0] != null && range[1] != null) {
            between(column, range[0], range[1]);
        }
        return this;
    }
}
