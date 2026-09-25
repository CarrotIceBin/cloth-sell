package com.clothsell.module.mall.enums;

import com.clothsell.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_002_001_000, "商品不存在");
    ErrorCode SKU_NOT_EXISTS = new ErrorCode(1_002_001_001, "规格不存在");
    ErrorCode SKU_IN_ORDER = new ErrorCode(1_002_001_002, "规格已有订单，不能删除");
    ErrorCode ORDER_NOT_EXISTS = new ErrorCode(1_002_001_010, "订单不存在");
    ErrorCode ORDER_STATUS_INVALID = new ErrorCode(1_002_001_011, "订单状态不能这样变更");
    ErrorCode STOCK_NOT_ENOUGH = new ErrorCode(1_002_001_012, "库存不足");
    ErrorCode CART_EMPTY = new ErrorCode(1_002_001_013, "购物车是空的");
    ErrorCode USER_EXISTS = new ErrorCode(1_002_001_020, "手机号已注册");
    ErrorCode LOGIN_BAD = new ErrorCode(1_002_001_021, "账号或密码错误");
    ErrorCode TOKEN_EXPIRED = new ErrorCode(1_002_001_022, "登录已过期");
    ErrorCode LOGIN_LOCKED = new ErrorCode(1_002_001_023, "尝试次数过多，请10分钟后再试");
    ErrorCode FILE_EMPTY = new ErrorCode(1_002_001_030, "请选择图片");
    ErrorCode FILE_UPLOAD = new ErrorCode(1_002_001_031, "图片上传失败");
    ErrorCode FILE_TYPE = new ErrorCode(1_002_001_032, "只支持 jpg、png、gif、webp 图片");
    ErrorCode COVER_BAD = new ErrorCode(1_002_001_033, "封面地址无效");
    ErrorCode MONEY_UNAVAILABLE = new ErrorCode(1_002_001_040, "金额计算服务不可用");
    ErrorCode JOURNAL_NOT_EXISTS = new ErrorCode(1_002_001_050, "文章不存在");
    ErrorCode REVIEW_NOT_EXISTS = new ErrorCode(1_002_001_060, "评价不存在");
    ErrorCode REVIEW_EXISTS = new ErrorCode(1_002_001_061, "你已经评价过这件商品了");
}
