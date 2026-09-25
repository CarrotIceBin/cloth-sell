package com.clothsell.module.mall.service.product;

import com.clothsell.framework.common.pojo.PageParam;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.dal.dataobject.product.ProductDO;
import com.clothsell.module.mall.dal.dataobject.product.ProductRespDTO;
import com.clothsell.module.mall.dal.dataobject.product.SkuDO;
import com.clothsell.module.mall.dal.mysql.cart.CartMapper;
import com.clothsell.module.mall.dal.mysql.order.OrderLineMapper;
import com.clothsell.module.mall.dal.mysql.product.ProductMapper;
import com.clothsell.module.mall.dal.mysql.product.SkuMapper;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.vo.product.ProductPageReqVO;
import com.clothsell.module.mall.vo.product.ProductSaveReqVO;
import com.clothsell.module.mall.vo.product.SkuSaveReqVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.SKU_IN_ORDER;

@Service
@Validated
public class ProductServiceImpl implements ProductService {
    public static final String CATEGORY = "服装";

    @Resource
    private ProductMapper productMapper;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private CartMapper cartMapper;
    @Resource
    private OrderLineMapper orderLineMapper;

    @Override
    @Transactional
    public Long createProduct(ProductSaveReqVO createReqVO) {
        ProductDO row = new ProductDO();
        row.setName(createReqVO.getName().trim());
        row.setCoverUrl(blank(createReqVO.getCoverUrl()));
        row.setOnShelf(createReqVO.getOnShelf());
        productMapper.insert(row);
        saveSkus(row.getId(), List.of(), createReqVO.getSkus());
        return row.getId();
    }

    @Override
    @Transactional
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        validateProductExists(updateReqVO.getId());
        ProductDO update = new ProductDO();
        update.setId(updateReqVO.getId());
        update.setName(updateReqVO.getName().trim());
        update.setCoverUrl(blank(updateReqVO.getCoverUrl()));
        update.setOnShelf(updateReqVO.getOnShelf());
        productMapper.updateById(update);
        List<SkuDO> existing = skuMapper.selectByProductIds(List.of(updateReqVO.getId()));
        saveSkus(updateReqVO.getId(), existing, updateReqVO.getSkus());
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        validateProductExists(id);
        List<SkuDO> skus = skuMapper.selectByProductIds(List.of(id));
        for (SkuDO sku : skus) {
            if (orderLineMapper.existsBySkuId(sku.getId())) {
                throw exception(SKU_IN_ORDER);
            }
            cartMapper.delete(new LambdaQueryWrapperX<com.clothsell.module.mall.dal.dataobject.cart.CartDO>()
                    .eq(com.clothsell.module.mall.dal.dataobject.cart.CartDO::getSkuId, sku.getId()));
            skuMapper.deleteById(sku.getId());
        }
        productMapper.deleteById(id);
    }

    @Override
    public ProductRespDTO getProduct(Long id) {
        ProductDO row = productMapper.selectById(id);
        if (row == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return assemble(List.of(row)).get(0);
    }

    @Override
    public PageResult<ProductRespDTO> getProductPage(ProductPageReqVO pageReqVO) {
        pageReqVO.toOrderBySql(PageParam.allow("name", "name", "createTime", "create_time", "id", "id"));
        PageResult<ProductDO> page = productMapper.selectPage(pageReqVO);
        return new PageResult<>(assemble(page.getList()), page.getTotal());
    }

    private void saveSkus(Long productId, List<SkuDO> existing, List<SkuSaveReqVO> rows) {
        Map<String, SkuDO> byKey = new LinkedHashMap<>();
        for (SkuDO sku : existing) {
            byKey.put(sku.getColor() + "/" + sku.getSize(), sku);
        }
        Set<String> seen = new HashSet<>();
        for (SkuSaveReqVO row : rows) {
            String key = row.getColor().trim() + "/" + row.getSize().trim();
            if (!seen.add(key)) {
                throw exception(SKU_IN_ORDER);
            }
            SkuDO sku = byKey.get(key);
            if (sku == null) {
                sku = new SkuDO();
                sku.setProductId(productId);
                sku.setColor(row.getColor().trim());
                sku.setSize(row.getSize().trim());
                sku.setPrice(row.getPrice());
                sku.setStock(row.getStock());
                skuMapper.insert(sku);
            } else {
                SkuDO update = new SkuDO();
                update.setId(sku.getId());
                update.setPrice(row.getPrice());
                update.setStock(row.getStock());
                skuMapper.updateById(update);
            }
        }
        for (SkuDO sku : existing) {
            if (seen.contains(sku.getColor() + "/" + sku.getSize())) {
                continue;
            }
            if (orderLineMapper.existsBySkuId(sku.getId())) {
                throw exception(SKU_IN_ORDER);
            }
            cartMapper.delete(new LambdaQueryWrapperX<com.clothsell.module.mall.dal.dataobject.cart.CartDO>()
                    .eq(com.clothsell.module.mall.dal.dataobject.cart.CartDO::getSkuId, sku.getId()));
            skuMapper.deleteById(sku.getId());
        }
    }

    private List<ProductRespDTO> assemble(List<ProductDO> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> ids = rows.stream().map(ProductDO::getId).toList();
        Map<Long, List<SkuDO>> skus = new LinkedHashMap<>();
        for (SkuDO sku : skuMapper.selectByProductIds(ids)) {
            skus.computeIfAbsent(sku.getProductId(), key -> new ArrayList<>()).add(sku);
        }
        List<ProductRespDTO> list = new ArrayList<>();
        for (ProductDO row : rows) {
            ProductRespDTO dto = BeanUtils.toBean(row, ProductRespDTO.class);
            dto.setCategory(CATEGORY);
            List<SkuDO> items = skus.getOrDefault(row.getId(), List.of());
            dto.setSkus(items);
            int stock = 0;
            BigDecimal min = null;
            for (SkuDO sku : items) {
                stock += sku.getStock();
                if (min == null || sku.getPrice().compareTo(min) < 0) {
                    min = sku.getPrice();
                }
            }
            dto.setStock(stock);
            dto.setMinPrice(min);
            list.add(dto);
        }
        return list;
    }

    private void validateProductExists(Long id) {
        if (id == null || productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
