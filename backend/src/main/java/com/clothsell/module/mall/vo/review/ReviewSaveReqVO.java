package com.clothsell.module.mall.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "评价提交 Request VO")
@Data
public class ReviewSaveReqVO {
    @Schema(description = "商品编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品不能为空")
    private Long productId;

    @Schema(description = "评分 1-5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择评分")
    @Min(value = 1, message = "评分最低 1 星")
    @Max(value = 5, message = "评分最高 5 星")
    private Integer rating;

    @Schema(description = "内容")
    @Size(max = 500, message = "评价最多 500 字")
    private String content;
}
