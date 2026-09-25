package com.clothsell.module.mall.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@TableName("mall_order")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDO extends BaseDO {
    @TableId
    private Long id;
    private Long userId;
    private String status;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String region;
    private String address;
    private BigDecimal freight;
    private BigDecimal totalAmount;
}
