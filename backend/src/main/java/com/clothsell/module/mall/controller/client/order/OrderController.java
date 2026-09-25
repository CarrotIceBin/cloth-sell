package com.clothsell.module.mall.controller.client.order;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.dal.dataobject.order.OrderRespDTO;
import com.clothsell.module.mall.service.order.OrderService;
import com.clothsell.module.mall.vo.order.OrderLineRespVO;
import com.clothsell.module.mall.vo.order.OrderPageReqVO;
import com.clothsell.module.mall.vo.order.OrderRespVO;
import com.clothsell.module.mall.vo.order.OrderSaveReqVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.framework.common.pojo.CommonResult.success;
import static com.clothsell.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.LOGIN_BAD;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.ORDER_NOT_EXISTS;

@RestController("clientOrderController")
@RequestMapping("/mall/client/order")
public class OrderController {
    @Resource
    private OrderService orderService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('mall:client-order:create')")
    public CommonResult<Long> createOrder(@Valid @RequestBody OrderSaveReqVO createReqVO) {
        Long userId = getLoginUserId();
        if (userId == null) {
            throw exception(LOGIN_BAD);
        }
        createReqVO.setUserId(userId);
        return success(orderService.createOrder(createReqVO));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('mall:client-order:query')")
    public CommonResult<PageResult<OrderRespVO>> getOrderPage(@Valid OrderPageReqVO pageReqVO) {
        pageReqVO.setUserId(getLoginUserId());
        PageResult<OrderRespDTO> page = orderService.getOrderPage(pageReqVO);
        return success(new PageResult<>(page.getList().stream().map(this::toVo).toList(), page.getTotal()));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('mall:client-order:query')")
    public CommonResult<OrderRespVO> getOrder(@RequestParam("id") Long id) {
        OrderRespDTO dto = orderService.getOrder(id);
        if (!dto.getUserId().equals(getLoginUserId())) {
            throw exception(ORDER_NOT_EXISTS);
        }
        return success(toVo(dto));
    }

    @PutMapping("/pay")
    @PreAuthorize("@ss.hasPermission('mall:client-order:update')")
    public CommonResult<Boolean> pay(@RequestParam("id") Long id) {
        orderService.payOrder(id, getLoginUserId());
        return success(true);
    }

    private OrderRespVO toVo(OrderRespDTO dto) {
        OrderRespVO vo = BeanUtils.toBean(dto, OrderRespVO.class);
        vo.setLines(BeanUtils.toBean(dto.getLines(), OrderLineRespVO.class));
        return vo;
    }
}
