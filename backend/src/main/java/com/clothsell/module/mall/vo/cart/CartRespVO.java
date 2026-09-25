package com.clothsell.module.mall.vo.cart;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartRespVO {
    private Long id;
    private Long skuId;
    private Long productId;
    private String name;
    private String coverUrl;
    private String color;
    private String size;
    private BigDecimal price;
    private BigDecimal amount;
    private Integer stock;
    private Integer qty;
}
