package com.clothsell.module.mall.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRespVO {
    private Long id;
    private Long userId;
    private String status;
    private String statusText;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String region;
    private String address;
    private BigDecimal freight;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
    private List<OrderLineRespVO> lines;
}
