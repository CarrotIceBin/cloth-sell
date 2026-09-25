package com.clothsell.module.mall.vo.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(description = "评价汇总 Response VO")
@Data
public class ReviewSummaryRespVO {
    @Schema(description = "评价数")
    private Long total;
    @Schema(description = "平均分，保留一位小数")
    private BigDecimal average;
    @Schema(description = "各星级数量，键是 5 到 1")
    private Map<Integer, Long> stars = new LinkedHashMap<>();
}
