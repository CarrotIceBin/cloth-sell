package com.clothsell.module.mall.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "评价 Response VO")
@Data
public class ReviewRespVO {
    @Schema(description = "编号")
    private Long id;
    @Schema(description = "商品编号")
    private Long productId;
    @Schema(description = "评价人")
    private String author;
    @Schema(description = "评分")
    private Integer rating;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "是否显示")
    private Boolean published;
    @Schema(description = "评价时间")
    private LocalDateTime createTime;
}
