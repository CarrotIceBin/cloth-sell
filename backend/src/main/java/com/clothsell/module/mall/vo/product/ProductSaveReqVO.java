package com.clothsell.module.mall.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "商品新增/修改 Request VO")
@Data
public class ProductSaveReqVO {
    @Schema(description = "编号")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "封面地址")
    private String coverUrl;

    @Schema(description = "是否上架", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "上架状态不能为空")
    private Boolean onShelf;

    @Valid
    @NotEmpty(message = "至少要有一个规格")
    private List<SkuSaveReqVO> skus;
}
