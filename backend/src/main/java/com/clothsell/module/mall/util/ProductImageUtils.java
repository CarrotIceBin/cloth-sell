package com.clothsell.module.mall.util;

public final class ProductImageUtils {
    private ProductImageUtils() {
    }

    public static String safeCover(String url) {
        if (url == null || url.length() > 500 || url.chars().anyMatch(ch -> ch <= ' ' || "\"'()<>\\".indexOf(ch) >= 0)) {
            return null;
        }
        if (url.startsWith("/files/") && url.matches("/files/[A-Za-z0-9._-]+")) {
            return url;
        }
        if (url.startsWith("https://")) {
            return url;
        }
        return null;
    }
}
