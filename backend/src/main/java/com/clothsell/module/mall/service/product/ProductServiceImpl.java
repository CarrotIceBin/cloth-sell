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
import com.clothsell.framework.redis.core.MallCache;
import com.clothsell.module.mall.service.money.MoneyClient;
import com.clothsell.module.mall.vo.product.SkuSaveReqVO;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.COVER_BAD;
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
    @Resource
    private MallCache mallCache;
    @Resource
    private MoneyClient moneyClient;

    @Override
    @Transactional
    public Long createProduct(ProductSaveReqVO createReqVO) {
        ProductDO row = new ProductDO();
        row.setName(createReqVO.getName().trim());
        row.setCoverUrl(cover(createReqVO.getCoverUrl()));
        row.setOnShelf(createReqVO.getOnShelf());
        productMapper.insert(row);
        saveSkus(row.getId(), List.of(), createReqVO.getSkus());
        mallCache.evictProducts();
        return row.getId();
    }

    @Override
    @Transactional
    public void updateProduct(ProductSaveReqVO updateReqVO) {
        validateProductExists(updateReqVO.getId());
        ProductDO update = new ProductDO();
        update.setId(updateReqVO.getId());
        update.setName(updateReqVO.getName().trim());
        update.setCoverUrl(cover(updateReqVO.getCoverUrl()));
        update.setOnShelf(updateReqVO.getOnShelf());
        productMapper.updateById(update);
        List<SkuDO> existing = skuMapper.selectByProductIds(List.of(updateReqVO.getId()));
        saveSkus(updateReqVO.getId(), existing, updateReqVO.getSkus());
        mallCache.evictProducts();
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
        mallCache.evictProducts();
    }

    @Override
    public ProductRespDTO getProduct(Long id) {
        String key = "mall:product:" + id;
        ProductRespDTO cached = mallCache.get(key, ProductRespDTO.class);
        if (cached != null) {
            return cached;
        }
        ProductDO row = productMapper.selectById(id);
        if (row == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        ProductRespDTO dto = assemble(List.of(row)).get(0);
        mallCache.set(key, dto, Duration.ofMinutes(10));
        return dto;
    }

    @Override
    public PageResult<ProductRespDTO> getProductPage(ProductPageReqVO pageReqVO) {
        pageReqVO.toOrderBySql(PageParam.allow("name", "name", "createTime", "create_time", "id", "id"));
        String key = "mall:product:page:" + pageReqVO.getPageNo() + ":" + pageReqVO.getPageSize()
                + ":" + pageReqVO.getName() + ":" + pageReqVO.getOnShelf()
                + ":" + pageReqVO.getSortBy() + ":" + pageReqVO.getSortOrder();
        PageResult<ProductRespDTO> cached = mallCache.get(key, new TypeReference<PageResult<ProductRespDTO>>() {
        });
        if (cached != null) {
            return cached;
        }
        PageResult<ProductDO> page = productMapper.selectPage(pageReqVO);
        PageResult<ProductRespDTO> result = new PageResult<>(assemble(page.getList()), page.getTotal());
        mallCache.set(key, result, Duration.ofMinutes(2));
        return result;
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
                sku.setCoverUrl(cover(row.getCoverUrl()));
                skuMapper.insert(sku);
            } else {
                SkuDO update = new SkuDO();
                update.setId(sku.getId());
                update.setPrice(row.getPrice());
                update.setStock(row.getStock());
                update.setCoverUrl(cover(row.getCoverUrl()));
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
            dto.setCoverUrl(safeCover(row.getCoverUrl()));
            dto.setCategory(CATEGORY);
            List<SkuDO> items = skus.getOrDefault(row.getId(), List.of());
            dto.setSkus(items);
            int stock = 0;
            for (SkuDO sku : items) {
                stock += sku.getStock();
            }
            dto.setStock(stock);
            list.add(dto);
        }
        try {
            Map<Long, BigDecimal> mins = moneyClient.minPrices(ids);
            for (ProductRespDTO dto : list) {
                dto.setMinPrice(mins.get(dto.getId()));
            }
        } catch (RuntimeException ex) {
            for (ProductRespDTO dto : list) {
                BigDecimal min = null;
                for (SkuDO sku : dto.getSkus()) {
                    if (sku.getPrice() == null) {
                        continue;
                    }
                    if (min == null || sku.getPrice().compareTo(min) < 0) {
                        min = sku.getPrice();
                    }
                }
                dto.setMinPrice(min);
            }
        }
        return list;
    }

    private void validateProductExists(Long id) {
        if (id == null || productMapper.selectById(id) == null) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
    }

    private String cover(String value) {
        String url = blank(value);
        if (url == null) {
            return null;
        }
        if (safeCover(url) == null) {
            throw exception(COVER_BAD);
        }
        return url;
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static String safeCover(String url) {
        if (url == null || url.length() > 500 || url.chars().anyMatch(ch -> ch <= ' ' || "\"'()<>\\".indexOf(ch) >= 0)) {
            return null;
        }
        if (url.startsWith("/files/") && url.matches("/files/[A-Za-z0-9._-]+")) {
            return url;
        }
        if (url.startsWith("https://")) {
            return url;
        }
        return null;
    }
}
