package com.clothsell.module.mall.dal.dataobject.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductRespDTO {
    private Long id;
    private String name;
    private String category;
    private String coverUrl;
    private Boolean onShelf;
    private BigDecimal minPrice;
    private Integer stock;
    private LocalDateTime createTime;
    private List<SkuDO> skus;
}
