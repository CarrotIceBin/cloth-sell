package com.clothsell.module.mall.vo.journal;

import com.clothsell.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "期刊分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class JournalPageReqVO extends PageParam {
    @Schema(description = "标题")
    private String title;
    @Schema(description = "分类标签")
    private String tag;
    @Schema(description = "是否发布")
    private Boolean published;
}
