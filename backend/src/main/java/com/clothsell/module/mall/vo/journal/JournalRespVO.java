package com.clothsell.module.mall.vo.journal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "期刊 Response VO")
@Data
public class JournalRespVO {
    @Schema(description = "编号")
    private Long id;
    @Schema(description = "分类标签")
    private String tag;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "摘要")
    private String summary;
    @Schema(description = "正文")
    private String content;
    @Schema(description = "封面地址")
    private String coverUrl;
    @Schema(description = "是否发布")
    private Boolean published;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
