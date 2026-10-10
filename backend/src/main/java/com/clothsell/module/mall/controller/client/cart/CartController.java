package com.clothsell.module.mall.controller.client.cart;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.service.cart.CartService;
import com.clothsell.module.mall.dal.dataobject.cart.CartListDTO;
import com.clothsell.module.mall.vo.cart.CartListRespVO;
import com.clothsell.module.mall.vo.cart.CartRespVO;
import com.clothsell.module.mall.vo.cart.CartSaveReqVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.pojo.CommonResult.success;
import static com.clothsell.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@RestController("clientCartController")
@RequestMapping("/mall/client/cart")
public class CartController {
    @Resource
    private CartService cartService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('mall:client-cart:create')")
    public CommonResult<Long> createCart(@Valid @RequestBody CartSaveReqVO createReqVO) {
        return success(cartService.createCart(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('mall:client-cart:update')")
    public CommonResult<Boolean> updateCart(@Valid @RequestBody CartSaveReqVO updateReqVO) {
        cartService.updateCart(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('mall:client-cart:delete')")
    public CommonResult<Boolean> deleteCart(@RequestParam("id") Long id) {
        cartService.deleteCart(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('mall:client-cart:query')")
    public CommonResult<CartListRespVO> getCartList() {
        CartListDTO dto = cartService.getCartList(getLoginUserId());
        CartListRespVO vo = new CartListRespVO();
        vo.setItems(BeanUtils.toBean(dto.getItems(), CartRespVO.class));
        vo.setFreight(dto.getFreight());
        vo.setPayable(dto.getPayable());
        return success(vo);
    }

}
