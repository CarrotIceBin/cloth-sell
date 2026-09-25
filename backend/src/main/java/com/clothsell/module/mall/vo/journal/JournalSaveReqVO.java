package com.clothsell.module.mall.vo.journal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "期刊新增/修改 Request VO")
@Data
public class JournalSaveReqVO {
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "分类标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "分类标签不能为空")
    @Size(max = 20, message = "分类标签过长")
    private String tag;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题过长")
    private String title;

    @Schema(description = "摘要")
    @Size(max = 255, message = "摘要过长")
    private String summary;

    @Schema(description = "正文")
    private String content;

    @Schema(description = "封面地址")
    private String coverUrl;

    @Schema(description = "是否发布", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发布状态不能为空")
    private Boolean published;
}
