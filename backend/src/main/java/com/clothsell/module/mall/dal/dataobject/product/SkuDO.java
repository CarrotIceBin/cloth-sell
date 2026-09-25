package com.clothsell.module.mall.dal.dataobject.product;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@TableName("mall_sku")
@Data
@EqualsAndHashCode(callSuper = true)
public class SkuDO extends BaseDO {
    @TableId
    private Long id;
    private Long productId;
    private String color;
    private String size;
    private BigDecimal price;
    private Integer stock;
    private String coverUrl;
}
