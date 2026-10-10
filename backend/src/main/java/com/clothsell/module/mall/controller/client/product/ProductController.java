package com.clothsell.module.mall.controller.client.product;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.dal.dataobject.product.ProductRespDTO;
import com.clothsell.module.mall.service.product.ProductService;
import com.clothsell.module.mall.vo.product.ProductPageReqVO;
import com.clothsell.module.mall.vo.product.ProductRespVO;
import com.clothsell.module.mall.vo.product.SkuRespVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.pojo.CommonResult.success;

@Tag(name = "顾客端 - 商品")
@RestController("clientProductController")
@RequestMapping("/mall/client/product")
public class ProductController {
    @Resource
    private ProductService productService;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('mall:client-product:query')")
    public CommonResult<PageResult<ProductRespVO>> getProductPage(@Valid ProductPageReqVO pageReqVO) {
        PageResult<ProductRespDTO> page = productService.getPublishedProductPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream().map(this::toVo).toList(), page.getTotal()));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('mall:client-product:query')")
    public CommonResult<ProductRespVO> getProduct(@RequestParam("id") Long id) {
        ProductRespDTO dto = productService.getPublishedProduct(id);
        return success(toVo(dto));
    }

    private ProductRespVO toVo(ProductRespDTO dto) {
        ProductRespVO vo = BeanUtils.toBean(dto, ProductRespVO.class);
        vo.setSkus(BeanUtils.toBean(dto.getSkus(), SkuRespVO.class));
        return vo;
    }
}
