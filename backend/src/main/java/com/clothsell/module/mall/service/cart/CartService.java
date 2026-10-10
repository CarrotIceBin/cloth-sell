package com.clothsell.module.mall.service.cart;

import com.clothsell.module.mall.dal.dataobject.cart.CartListDTO;
import com.clothsell.module.mall.vo.cart.CartSaveReqVO;
import jakarta.validation.Valid;

public interface CartService {
    Long createCart(@Valid CartSaveReqVO createReqVO, Long userId);

    void updateCart(@Valid CartSaveReqVO updateReqVO, Long userId);

    void deleteCart(Long id, Long userId);

    CartListDTO getCartList(Long userId);
}
