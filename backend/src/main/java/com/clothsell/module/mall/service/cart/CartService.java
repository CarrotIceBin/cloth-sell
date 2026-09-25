package com.clothsell.module.mall.service.cart;

import com.clothsell.module.mall.dal.dataobject.cart.CartRespDTO;
import com.clothsell.module.mall.vo.cart.CartSaveReqVO;
import jakarta.validation.Valid;

import java.util.List;

public interface CartService {
    Long createCart(@Valid CartSaveReqVO createReqVO);

    void updateCart(@Valid CartSaveReqVO updateReqVO);

    void deleteCart(Long id, Long userId);

    List<CartRespDTO> getCartList(Long userId);
}
