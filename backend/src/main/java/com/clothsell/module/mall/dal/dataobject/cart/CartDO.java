package com.clothsell.module.mall.dal.dataobject.cart;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("mall_cart")
@Data
@EqualsAndHashCode(callSuper = true)
public class CartDO extends BaseDO {
    @TableId
    private Long id;
    private Long userId;
    private Long skuId;
    private Integer qty;
}
