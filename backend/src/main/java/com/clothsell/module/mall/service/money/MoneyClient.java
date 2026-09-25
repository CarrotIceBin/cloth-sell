package com.clothsell.module.mall.service.money;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.MONEY_UNAVAILABLE;

@Component
public class MoneyClient {
    private final RestClient http;

    public MoneyClient(@Value("${app.money-url:http://127.0.0.1:5088}") String baseUrl) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    public CartAmounts cart(Long userId) {
        CartResp body = post("/cart/" + userId + "/amounts", null, CartResp.class);
        if (body == null || body.items() == null || body.payable() == null) {
            throw exception(MONEY_UNAVAILABLE);
        }
        Map<Long, BigDecimal> amounts = new HashMap<>();
        for (CartItem item : body.items()) {
            if (item.cartId() == null || item.amount() == null) {
                throw exception(MONEY_UNAVAILABLE);
            }
            amounts.put(item.cartId(), money(item.amount()));
        }
        return new CartAmounts(amounts, money(body.freight()), money(body.payable()));
    }

    public Map<Long, BigDecimal> minPrices(List<Long> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        MinResp body = post("/products/min-prices", new IdList(productIds), MinResp.class);
        if (body == null || body.items() == null) {
            throw exception(MONEY_UNAVAILABLE);
        }
        Map<Long, BigDecimal> mins = new HashMap<>();
        for (MinItem item : body.items()) {
            if (item.productId() == null || item.minPrice() == null) {
                throw exception(MONEY_UNAVAILABLE);
            }
            mins.put(item.productId(), money(item.minPrice()));
        }
        return mins;
    }

    public void fillOrder(Long orderId) {
        FillResp body = post("/orders/" + orderId + "/amounts", null, FillResp.class);
        if (body == null || body.payable() == null) {
            throw exception(MONEY_UNAVAILABLE);
        }
    }

    private <T> T post(String path, Object body, Class<T> type) {
        try {
            var request = http.post().uri(path);
            if (body != null) {
                request.body(body);
            }
            return request.retrieve().body(type);
        } catch (RestClientException ex) {
            throw exception(MONEY_UNAVAILABLE);
        }
    }

    private static BigDecimal money(String text) {
        if (text == null || text.isBlank()) {
            throw exception(MONEY_UNAVAILABLE);
        }
        return new BigDecimal(text);
    }

    public record CartAmounts(Map<Long, BigDecimal> amounts, BigDecimal freight, BigDecimal payable) {
    }

    private record CartItem(Long cartId, String price, String amount) {
    }

    private record CartResp(List<CartItem> items, String freight, String payable) {
    }

    private record IdList(List<Long> productIds) {
    }

    private record MinItem(Long productId, String minPrice) {
    }

    private record MinResp(List<MinItem> items) {
    }

    private record FillResp(String freight, String payable) {
    }
}
