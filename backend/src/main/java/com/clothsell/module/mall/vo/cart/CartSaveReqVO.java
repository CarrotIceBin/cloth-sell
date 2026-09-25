package com.clothsell.module.mall.vo.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartSaveReqVO {
    private Long id;
    private Long userId;
    private Long skuId;
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer qty;
}
