package com.clothsell.module.mall.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@TableName("mall_order_line")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderLineDO extends BaseDO {
    @TableId
    private Long id;
    private Long orderId;
    private Long skuId;
    private String productName;
    private String color;
    private String size;
    private BigDecimal price;
    private Integer qty;
}
