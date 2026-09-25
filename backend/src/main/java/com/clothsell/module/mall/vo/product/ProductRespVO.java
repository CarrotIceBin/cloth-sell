package com.clothsell.module.mall.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "商品 Response VO")
@Data
public class ProductRespVO {
    private Long id;
    private String name;
    private String category;
    private String coverUrl;
    private Boolean onShelf;
    private BigDecimal minPrice;
    private Integer stock;
    private LocalDateTime createTime;
    private List<SkuRespVO> skus;
}
