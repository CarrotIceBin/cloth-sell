package com.clothsell.module.mall.service.order;

import com.clothsell.framework.common.pojo.PageParam;
import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.common.util.object.BeanUtils;
import com.clothsell.module.mall.dal.dataobject.cart.CartDO;
import com.clothsell.module.mall.dal.dataobject.order.OrderDO;
import com.clothsell.module.mall.dal.dataobject.order.OrderLineDO;
import com.clothsell.module.mall.dal.dataobject.order.OrderRespDTO;
import com.clothsell.module.mall.dal.dataobject.product.ProductDO;
import com.clothsell.module.mall.dal.dataobject.product.SkuDO;
import com.clothsell.module.mall.dal.mysql.cart.CartMapper;
import com.clothsell.module.mall.dal.mysql.order.OrderLineMapper;
import com.clothsell.module.mall.dal.mysql.order.OrderMapper;
import com.clothsell.module.mall.dal.mysql.product.ProductMapper;
import com.clothsell.module.mall.dal.mysql.product.SkuMapper;
import com.clothsell.module.mall.vo.order.OrderPageReqVO;
import com.clothsell.module.mall.vo.order.OrderSaveReqVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.CART_EMPTY;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.ORDER_NOT_EXISTS;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.ORDER_STATUS_INVALID;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.STOCK_NOT_ENOUGH;

@Service
@Validated
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderLineMapper orderLineMapper;
    @Resource
    private CartMapper cartMapper;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private ProductMapper productMapper;

    @Override
    @Transactional
    public Long createOrder(OrderSaveReqVO createReqVO) {
        List<CartDO> cart = cartMapper.selectByUserId(createReqVO.getUserId());
        if (cart.isEmpty()) {
            throw exception(CART_EMPTY);
        }
        OrderDO order = new OrderDO();
        order.setUserId(createReqVO.getUserId());
        order.setStatus("PENDING");
        order.setReceiverName(createReqVO.getReceiverName().trim());
        order.setReceiverPhone(createReqVO.getReceiverPhone());
        String province = createReqVO.getProvince().trim();
        String city = createReqVO.getCity().trim();
        String district = createReqVO.getDistrict().trim();
        order.setProvince(province);
        order.setCity(city);
        order.setDistrict(district);
        order.setRegion(province + city + district);
        order.setAddress(createReqVO.getAddress().trim());
        order.setFreight(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);
        BigDecimal total = BigDecimal.ZERO;
        for (CartDO item : cart) {
            SkuDO sku = skuMapper.selectForUpdate(item.getSkuId());
            if (sku == null) {
                throw exception(SKU_NOT_EXISTS);
            }
            ProductDO product = productMapper.selectById(sku.getProductId());
            if (product == null || !Boolean.TRUE.equals(product.getOnShelf())) {
                throw exception(PRODUCT_NOT_EXISTS);
            }
            if (sku.getStock() < item.getQty()) {
                throw exception(STOCK_NOT_ENOUGH);
            }
            SkuDO stock = new SkuDO();
            stock.setId(sku.getId());
            stock.setStock(sku.getStock() - item.getQty());
            skuMapper.updateById(stock);
            OrderLineDO line = new OrderLineDO();
            line.setOrderId(order.getId());
            line.setSkuId(sku.getId());
            line.setProductName(product.getName());
            line.setColor(sku.getColor());
            line.setSize(sku.getSize());
            line.setPrice(sku.getPrice());
            line.setQty(item.getQty());
            orderLineMapper.insert(line);
            total = total.add(sku.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
            cartMapper.deleteById(item.getId());
        }
        OrderDO amount = new OrderDO();
        amount.setId(order.getId());
        amount.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        orderMapper.updateById(amount);
        return order.getId();
    }

    @Override
    public void payOrder(Long id, Long userId) {
        OrderDO current = orderMapper.selectById(id);
        if (current == null || !current.getUserId().equals(userId)) {
            throw exception(ORDER_NOT_EXISTS);
        }
        if (!"PENDING".equals(current.getStatus())) {
            throw exception(ORDER_STATUS_INVALID);
        }
        OrderDO update = new OrderDO();
        update.setId(id);
        update.setStatus("PAID");
        orderMapper.updateById(update);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, String status) {
        OrderDO current = orderMapper.selectById(id);
        if (current == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        if (!allowed(current.getStatus(), status)) {
            throw exception(ORDER_STATUS_INVALID);
        }
        if ("CANCELLED".equals(status)) {
            for (OrderLineDO line : orderLineMapper.selectByOrderIds(List.of(id))) {
                SkuDO sku = line.getSkuId() == null ? null : skuMapper.selectForUpdate(line.getSkuId());
                if (sku == null) {
                    continue;
                }
                SkuDO update = new SkuDO();
                update.setId(sku.getId());
                update.setStock(sku.getStock() + line.getQty());
                skuMapper.updateById(update);
            }
        }
        OrderDO update = new OrderDO();
        update.setId(id);
        update.setStatus(status);
        orderMapper.updateById(update);
    }

    @Override
    public OrderRespDTO getOrder(Long id) {
        OrderDO row = orderMapper.selectById(id);
        if (row == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        return assemble(List.of(row)).get(0);
    }

    @Override
    public PageResult<OrderRespDTO> getOrderPage(OrderPageReqVO pageReqVO) {
        pageReqVO.toOrderBySql(PageParam.allow("createTime", "create_time", "id", "id", "totalAmount", "total_amount"));
        PageResult<OrderDO> page = orderMapper.selectPage(pageReqVO);
        return new PageResult<>(assemble(page.getList()), page.getTotal());
    }

    private boolean allowed(String now, String next) {
        if ("PENDING".equals(now)) {
            return "PAID".equals(next) || "CANCELLED".equals(next);
        }
        if ("PAID".equals(now)) {
            return "SHIPPED".equals(next) || "CANCELLED".equals(next);
        }
        if ("SHIPPED".equals(now)) {
            return "DONE".equals(next);
        }
        return false;
    }

    private List<OrderRespDTO> assemble(List<OrderDO> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, List<OrderLineDO>> lines = new LinkedHashMap<>();
        for (OrderLineDO line : orderLineMapper.selectByOrderIds(rows.stream().map(OrderDO::getId).toList())) {
            lines.computeIfAbsent(line.getOrderId(), key -> new ArrayList<>()).add(line);
        }
        List<OrderRespDTO> list = new ArrayList<>();
        for (OrderDO row : rows) {
            OrderRespDTO dto = BeanUtils.toBean(row, OrderRespDTO.class);
            dto.setStatusText(label(row.getStatus()));
            dto.setLines(lines.getOrDefault(row.getId(), List.of()));
            list.add(dto);
        }
        return list;
    }

    public static String label(String status) {
        return switch (status) {
            case "PENDING" -> "待付款";
            case "PAID" -> "已付款";
            case "SHIPPED" -> "已发货";
            case "DONE" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }
}
