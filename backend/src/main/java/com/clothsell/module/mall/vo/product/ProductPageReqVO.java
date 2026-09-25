package com.clothsell.module.mall.vo.product;

import com.clothsell.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "商品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductPageReqVO extends PageParam {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "是否上架")
    private Boolean onShelf;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime[] createTime;
}
