package com.clothsell.module.mall.dal.dataobject.product;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("mall_product")
@KeySequence("mall_product_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductDO extends BaseDO {
    @TableId
    private Long id;
    private String name;
    private String coverUrl;
    private Boolean onShelf;
}
