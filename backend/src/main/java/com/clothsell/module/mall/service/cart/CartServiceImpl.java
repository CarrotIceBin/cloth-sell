package com.clothsell.module.mall.service.cart;

import com.clothsell.framework.common.exception.ServiceException;
import com.clothsell.module.mall.dal.dataobject.cart.CartDO;
import com.clothsell.module.mall.dal.dataobject.cart.CartListDTO;
import com.clothsell.module.mall.dal.dataobject.cart.CartRespDTO;
import com.clothsell.module.mall.dal.dataobject.product.ProductDO;
import com.clothsell.module.mall.dal.dataobject.product.SkuDO;
import com.clothsell.module.mall.dal.mysql.cart.CartMapper;
import com.clothsell.module.mall.dal.mysql.product.ProductMapper;
import com.clothsell.module.mall.dal.mysql.product.SkuMapper;
import com.clothsell.module.mall.service.money.MoneyClient;
import com.clothsell.module.mall.service.money.MoneyClient.CartAmounts;
import com.clothsell.module.mall.service.product.ProductServiceImpl;
import com.clothsell.module.mall.vo.cart.CartSaveReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.MONEY_UNAVAILABLE;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.ORDER_NOT_EXISTS;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.STOCK_NOT_ENOUGH;

@Slf4j
@Service
@Validated
public class CartServiceImpl implements CartService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    @Resource
    private CartMapper cartMapper;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private MoneyClient moneyClient;

    @Override
    public Long createCart(CartSaveReqVO createReqVO) {
        SkuDO sku = requireShelfSku(createReqVO.getSkuId());
        CartDO existing = cartMapper.selectByUserAndSku(createReqVO.getUserId(), sku.getId());
        int next = createReqVO.getQty() + (existing == null ? 0 : existing.getQty());
        if (next > sku.getStock()) {
            throw exception(STOCK_NOT_ENOUGH);
        }
        if (existing == null) {
            CartDO row = new CartDO();
            row.setUserId(createReqVO.getUserId());
            row.setSkuId(sku.getId());
            row.setQty(next);
            cartMapper.insert(row);
            return row.getId();
        }
        CartDO update = new CartDO();
        update.setId(existing.getId());
        update.setQty(next);
        cartMapper.updateById(update);
        return existing.getId();
    }

    @Override
    public void updateCart(CartSaveReqVO updateReqVO) {
        CartDO current = cartMapper.selectById(updateReqVO.getId());
        if (current == null || !current.getUserId().equals(updateReqVO.getUserId())) {
            throw exception(ORDER_NOT_EXISTS);
        }
        SkuDO sku = skuMapper.selectById(current.getSkuId());
        if (sku == null || updateReqVO.getQty() > sku.getStock()) {
            throw exception(STOCK_NOT_ENOUGH);
        }
        CartDO update = new CartDO();
        update.setId(current.getId());
        update.setQty(updateReqVO.getQty());
        cartMapper.updateById(update);
    }

    @Override
    public void deleteCart(Long id, Long userId) {
        CartDO current = cartMapper.selectById(id);
        if (current == null || !current.getUserId().equals(userId)) {
            throw exception(ORDER_NOT_EXISTS);
        }
        cartMapper.deleteById(id);
    }

    @Override
    public CartListDTO getCartList(Long userId) {
        List<CartRespDTO> list = new ArrayList<>();
        for (CartDO item : cartMapper.selectByUserId(userId)) {
            SkuDO sku = skuMapper.selectById(item.getSkuId());
            ProductDO product = sku == null ? null : productMapper.selectById(sku.getProductId());
            if (sku == null || product == null) {
                continue;
            }
            CartRespDTO dto = new CartRespDTO();
            dto.setId(item.getId());
            dto.setSkuId(sku.getId());
            dto.setProductId(product.getId());
            dto.setName(product.getName());
            dto.setCoverUrl(colorCover(sku, product));
            dto.setColor(sku.getColor());
            dto.setSize(sku.getSize());
            dto.setPrice(sku.getPrice());
            dto.setStock(sku.getStock());
            dto.setQty(item.getQty());
            list.add(dto);
        }
        CartListDTO result = new CartListDTO();
        result.setItems(list);
        if (list.isEmpty()) {
            result.setFreight(ZERO);
            result.setPayable(ZERO);
            return result;
        }
        try {
            CartAmounts amounts = moneyClient.cart(userId);
            for (CartRespDTO item : list) {
                BigDecimal amount = amounts.amounts().get(item.getId());
                if (amount == null) {
                    throw exception(MONEY_UNAVAILABLE);
                }
                item.setAmount(amount);
            }
            result.setFreight(amounts.freight());
            result.setPayable(amounts.payable());
        } catch (ServiceException ex) {
            if (!MONEY_UNAVAILABLE.getCode().equals(ex.getCode())) {
                throw ex;
            }
            log.warn("金额服务不可用，购物车金额改由本地计算：userId={}", userId);
            BigDecimal payable = ZERO;
            for (CartRespDTO item : list) {
                BigDecimal amount = lineAmount(item.getPrice(), item.getQty());
                item.setAmount(amount);
                payable = payable.add(amount);
            }
            result.setFreight(ZERO);
            result.setPayable(payable.setScale(2, RoundingMode.HALF_UP));
        }
        return result;
    }

    private static BigDecimal lineAmount(BigDecimal price, Integer qty) {
        if (price == null || qty == null) {
            return ZERO;
        }
        return price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
    }

    private String colorCover(SkuDO sku, ProductDO product) {
        String cover = ProductServiceImpl.safeCover(sku.getCoverUrl());
        return cover != null ? cover : ProductServiceImpl.safeCover(product.getCoverUrl());
    }

    private SkuDO requireShelfSku(Long skuId) {
        SkuDO sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
        ProductDO product = productMapper.selectById(sku.getProductId());
        if (product == null || !Boolean.TRUE.equals(product.getOnShelf())) {
            throw exception(PRODUCT_NOT_EXISTS);
        }
        return sku;
    }
}
