package com.clothsell.module.mall.vo.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuRespVO {
    private Long id;
    private String color;
    private String size;
    private BigDecimal price;
    private Integer stock;
    private String coverUrl;
}
