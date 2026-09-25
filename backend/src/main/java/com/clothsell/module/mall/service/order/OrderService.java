package com.clothsell.module.mall.service.order;

import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.module.mall.dal.dataobject.order.OrderRespDTO;
import com.clothsell.module.mall.vo.order.OrderPageReqVO;
import com.clothsell.module.mall.vo.order.OrderSaveReqVO;
import jakarta.validation.Valid;

public interface OrderService {
    Long createOrder(@Valid OrderSaveReqVO createReqVO);

    void payOrder(Long id, Long userId);

    void updateOrderStatus(Long id, String status);

    OrderRespDTO getOrder(Long id);

    PageResult<OrderRespDTO> getOrderPage(OrderPageReqVO pageReqVO);
}
