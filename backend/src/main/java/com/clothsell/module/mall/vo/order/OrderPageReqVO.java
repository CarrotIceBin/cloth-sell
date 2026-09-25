package com.clothsell.module.mall.vo.order;

import com.clothsell.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageReqVO extends PageParam {
    private String status;
    private Long userId;
    private Long authUserId;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime[] createTime;
}
