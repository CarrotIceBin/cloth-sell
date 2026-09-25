package com.clothsell.module.mall.vo.review;

import com.clothsell.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "评价分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewPageReqVO extends PageParam {
    @Schema(description = "商品编号")
    private Long productId;
    @Schema(description = "评分")
    private Integer rating;
    @Schema(description = "是否显示")
    private Boolean published;
}
