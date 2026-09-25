package com.clothsell.framework.common.pojo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PageParam {
    public static final Integer PAGE_SIZE_NONE = -1;

    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy;
    private String sortOrder;
    private String orderBySql;

    public String toOrderBySql(Map<String, String> allowed) {
        orderBySql = null;
        if (sortBy == null || sortOrder == null || allowed == null) {
            return null;
        }
        String column = allowed.get(sortBy);
        if (column == null) {
            return null;
        }
        if (!"asc".equals(sortOrder) && !"desc".equals(sortOrder)) {
            return null;
        }
        orderBySql = column + " " + sortOrder;
        return orderBySql;
    }

    public static Map<String, String> allow(String... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
