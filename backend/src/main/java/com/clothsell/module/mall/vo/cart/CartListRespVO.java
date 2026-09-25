package com.clothsell.module.mall.vo.cart;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartListRespVO {
    private List<CartRespVO> items;
    private BigDecimal freight;
    private BigDecimal payable;
}
