package com.clothsell.module.mall.dal.dataobject.review;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("mall_review")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewDO extends BaseDO {
    @TableId
    private Long id;
    private Long productId;
    private Long userId;
    private String author;
    private Integer rating;
    private String content;
    private Boolean published;
}
