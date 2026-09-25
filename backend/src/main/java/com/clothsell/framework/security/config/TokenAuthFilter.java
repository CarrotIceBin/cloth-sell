package com.clothsell.framework.security.config;

import com.clothsell.framework.security.core.TokenService;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {
    private final TokenService tokens;

    public TokenAuthFilter(TokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String raw = header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
        LoginUser user = tokens.parse(raw);
        if (user != null) {
            var authorities = "ADMIN".equals(user.role()) ? adminPerms() : clientPerms();
            var auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if ("GET".equals(request.getMethod()) && guestReadable(request.getRequestURI())) {
            var auth = new AnonymousAuthenticationToken("guest", "guest",
                    List.of(new SimpleGrantedAuthority("mall:client-product:query"),
                            new SimpleGrantedAuthority("mall:client-journal:query"),
                            new SimpleGrantedAuthority("mall:client-review:query")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean guestReadable(String uri) {
        return uri.startsWith("/mall/client/product")
                || uri.startsWith("/mall/client/journal")
                || uri.startsWith("/mall/client/review");
    }

    private List<SimpleGrantedAuthority> adminPerms() {
        return List.of(
                new SimpleGrantedAuthority("mall:product:query"),
                new SimpleGrantedAuthority("mall:product:create"),
                new SimpleGrantedAuthority("mall:product:update"),
                new SimpleGrantedAuthority("mall:product:delete"),
                new SimpleGrantedAuthority("mall:order:query"),
                new SimpleGrantedAuthority("mall:order:update"),
                new SimpleGrantedAuthority("mall:journal:query"),
                new SimpleGrantedAuthority("mall:journal:create"),
                new SimpleGrantedAuthority("mall:journal:update"),
                new SimpleGrantedAuthority("mall:journal:delete"),
                new SimpleGrantedAuthority("mall:review:query"),
                new SimpleGrantedAuthority("mall:review:update"),
                new SimpleGrantedAuthority("mall:review:delete")
        );
    }

    private List<SimpleGrantedAuthority> clientPerms() {
        return List.of(
                new SimpleGrantedAuthority("mall:client-product:query"),
                new SimpleGrantedAuthority("mall:client-cart:query"),
                new SimpleGrantedAuthority("mall:client-cart:create"),
                new SimpleGrantedAuthority("mall:client-cart:update"),
                new SimpleGrantedAuthority("mall:client-cart:delete"),
                new SimpleGrantedAuthority("mall:client-order:query"),
                new SimpleGrantedAuthority("mall:client-order:create"),
                new SimpleGrantedAuthority("mall:client-order:update"),
                new SimpleGrantedAuthority("mall:client-journal:query"),
                new SimpleGrantedAuthority("mall:client-review:query"),
                new SimpleGrantedAuthority("mall:client-review:create")
        );
    }
}
