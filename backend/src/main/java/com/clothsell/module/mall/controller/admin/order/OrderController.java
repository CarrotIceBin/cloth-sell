package com.clothsell.module.mall.controller.admin.order;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.service.order.OrderService;
import com.clothsell.module.mall.vo.order.OrderLineRespVO;
import com.clothsell.module.mall.vo.order.OrderPageReqVO;
import com.clothsell.module.mall.vo.order.OrderRespVO;
import com.clothsell.module.mall.vo.order.OrderStatusReqVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.pojo.CommonResult.success;
import static com.clothsell.framework.security.core.util.SecurityFrameworkUtils.getAuthId;

@Tag(name = "管理后台 - 订单")
@RestController
@RequestMapping("/mall/order")
public class OrderController {
    @Resource
    private OrderService orderService;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('mall:order:query')")
    public CommonResult<PageResult<OrderRespVO>> getOrderPage(@Valid OrderPageReqVO pageReqVO) {
        pageReqVO.setAuthUserId(getAuthId());
        pageReqVO.setUserId(null);
        return success(toPage(orderService.getOrderPage(pageReqVO)));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('mall:order:query')")
    public CommonResult<OrderRespVO> getOrder(@RequestParam("id") Long id) {
        return success(toVo(orderService.getOrder(id)));
    }

    @PutMapping("/update-status")
    @PreAuthorize("@ss.hasPermission('mall:order:update')")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody OrderStatusReqVO reqVO) {
        orderService.updateOrderStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    private PageResult<OrderRespVO> toPage(PageResult<com.clothsell.module.mall.dal.dataobject.order.OrderRespDTO> page) {
        return new PageResult<>(page.getList().stream().map(this::toVo).toList(), page.getTotal());
    }

    private OrderRespVO toVo(com.clothsell.module.mall.dal.dataobject.order.OrderRespDTO dto) {
        OrderRespVO vo = BeanUtils.toBean(dto, OrderRespVO.class);
        vo.setLines(BeanUtils.toBean(dto.getLines(), OrderLineRespVO.class));
        return vo;
    }
}
