package com.clothsell.module.mall.vo.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuSaveReqVO {
    @NotBlank(message = "颜色不能为空")
    private String color;
    @NotBlank(message = "尺码不能为空")
    private String size;
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负")
    private Integer stock;
    @Size(max = 255, message = "图片地址过长")
    private String coverUrl;
}
