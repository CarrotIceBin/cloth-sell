package com.clothsell.framework.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiPrefixFilter extends OncePerRequestFilter {
    static final String ADMIN = "/admin-api";
    static final String CLIENT = "/client-api";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String prefix = prefixOf(uri);
        if (prefix == null) {
            chain.doFilter(request, response);
            return;
        }
        String path = uri.substring(prefix.length());
        if (!allowed(prefix, path)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(new PrefixedRequest(request, path), response);
    }

    private static String prefixOf(String uri) {
        if (startsWithPrefix(uri, ADMIN)) {
            return ADMIN;
        }
        if (startsWithPrefix(uri, CLIENT)) {
            return CLIENT;
        }
        return null;
    }

    private static boolean startsWithPrefix(String uri, String prefix) {
        return uri.startsWith(prefix) && (uri.length() == prefix.length() || uri.charAt(prefix.length()) == '/');
    }

    static boolean allowed(String prefix, String path) {
        if (ADMIN.equals(prefix)) {
            return path.startsWith("/mall/product")
                    || path.startsWith("/mall/order")
                    || path.startsWith("/mall/file")
                    || "/mall/auth/admin-login".equals(path)
                    || "/mall/auth/refresh".equals(path)
                    || "/mall/auth/logout".equals(path);
        }
        return path.startsWith("/mall/client")
                || "/mall/auth/register".equals(path)
                || "/mall/auth/login".equals(path)
                || "/mall/auth/refresh".equals(path)
                || "/mall/auth/logout".equals(path);
    }

    private static final class PrefixedRequest extends HttpServletRequestWrapper {
        private final String path;

        private PrefixedRequest(HttpServletRequest request, String path) {
            super(request);
            this.path = path.isEmpty() ? "/" : path;
        }

        @Override
        public String getRequestURI() {
            return getContextPath() + path;
        }

        @Override
        public String getServletPath() {
            return path;
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            url.append(getScheme()).append("://").append(getServerName());
            int port = getServerPort();
            if (port != 80 && port != 443) {
                url.append(':').append(port);
            }
            url.append(getRequestURI());
            return url;
        }
    }
}
