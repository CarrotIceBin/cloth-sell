package com.clothsell.module.mall.vo.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLineRespVO {
    private String productName;
    private String color;
    private String size;
    private BigDecimal price;
    private Integer qty;
}
