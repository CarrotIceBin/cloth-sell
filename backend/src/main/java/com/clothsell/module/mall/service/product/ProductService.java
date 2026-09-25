package com.clothsell.module.mall.service.product;

import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.module.mall.dal.dataobject.product.ProductRespDTO;
import com.clothsell.module.mall.vo.product.ProductPageReqVO;
import com.clothsell.module.mall.vo.product.ProductSaveReqVO;
import jakarta.validation.Valid;

public interface ProductService {
    Long createProduct(@Valid ProductSaveReqVO createReqVO);

    void updateProduct(@Valid ProductSaveReqVO updateReqVO);

    void deleteProduct(Long id);

    ProductRespDTO getProduct(Long id);

    PageResult<ProductRespDTO> getProductPage(ProductPageReqVO pageReqVO);
}
