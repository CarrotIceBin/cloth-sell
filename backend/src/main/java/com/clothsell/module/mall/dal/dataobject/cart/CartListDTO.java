package com.clothsell.module.mall.dal.dataobject.cart;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartListDTO {
    private List<CartRespDTO> items;
    private BigDecimal freight;
    private BigDecimal payable;
}
