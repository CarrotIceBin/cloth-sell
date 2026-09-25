package com.clothsell.module.mall.vo.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusReqVO {
    @NotNull(message = "编号不能为空")
    private Long id;
    @NotBlank(message = "状态不能为空")
    private String status;
}
