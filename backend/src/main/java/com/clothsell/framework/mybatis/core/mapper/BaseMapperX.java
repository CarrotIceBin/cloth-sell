package com.clothsell.framework.mybatis.core.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clothsell.framework.common.pojo.PageParam;
import com.clothsell.framework.common.pojo.PageResult;

import java.util.List;

public interface BaseMapperX<T> extends BaseMapper<T> {
    default PageResult<T> selectPage(PageParam pageParam, Wrapper<T> wrapper) {
        if (PageParam.PAGE_SIZE_NONE.equals(pageParam.getPageSize())) {
            List<T> list = selectList(wrapper);
            return new PageResult<>(list, (long) list.size());
        }
        long pageNo = pageParam.getPageNo() == null ? 1 : pageParam.getPageNo();
        long pageSize = pageParam.getPageSize() == null ? 10 : pageParam.getPageSize();
        Page<T> page = selectPage(new Page<>(pageNo, pageSize), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }
}
