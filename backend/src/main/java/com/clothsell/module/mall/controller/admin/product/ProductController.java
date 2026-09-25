package com.clothsell.module.mall.controller.admin.product;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.dal.dataobject.product.ProductRespDTO;
import com.clothsell.module.mall.service.product.ProductService;
import com.clothsell.module.mall.vo.product.ProductPageReqVO;
import com.clothsell.module.mall.vo.product.ProductRespVO;
import com.clothsell.module.mall.vo.product.ProductSaveReqVO;
import com.clothsell.module.mall.vo.product.SkuRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.pojo.CommonResult.success;
import static com.clothsell.framework.security.core.util.SecurityFrameworkUtils.getAuthId;

@Tag(name = "管理后台 - 商品")
@RestController
@RequestMapping("/mall/product")
@Validated
public class ProductController {
    @Resource
    private ProductService productService;

    @PostMapping("/create")
    @Operation(summary = "创建商品")
    @PreAuthorize("@ss.hasPermission('mall:product:create')")
    public CommonResult<Long> createProduct(@Valid @RequestBody ProductSaveReqVO createReqVO) {
        getAuthId();
        return success(productService.createProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品")
    @PreAuthorize("@ss.hasPermission('mall:product:update')")
    public CommonResult<Boolean> updateProduct(@Valid @RequestBody ProductSaveReqVO updateReqVO) {
        productService.updateProduct(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商品")
    @PreAuthorize("@ss.hasPermission('mall:product:delete')")
    public CommonResult<Boolean> deleteProduct(@RequestParam("id") Long id) {
        productService.deleteProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "商品详情")
    @PreAuthorize("@ss.hasPermission('mall:product:query')")
    public CommonResult<ProductRespVO> getProduct(@RequestParam("id") Long id) {
        return success(toVo(productService.getProduct(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "商品分页")
    @PreAuthorize("@ss.hasPermission('mall:product:query')")
    public CommonResult<PageResult<ProductRespVO>> getProductPage(@Valid ProductPageReqVO pageReqVO) {
        PageResult<ProductRespDTO> page = productService.getProductPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream().map(this::toVo).toList(), page.getTotal()));
    }

    private ProductRespVO toVo(ProductRespDTO dto) {
        ProductRespVO vo = BeanUtils.toBean(dto, ProductRespVO.class);
        vo.setSkus(BeanUtils.toBean(dto.getSkus(), SkuRespVO.class));
        return vo;
    }
}
