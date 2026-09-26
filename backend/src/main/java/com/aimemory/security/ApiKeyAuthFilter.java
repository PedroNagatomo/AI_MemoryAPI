package com.aimemory.security;

import com.aimemory.entity.Tenant;
import com.aimemory.repository.TenantRepository;
import com.aimemory.util.ApiKeyGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final TenantRepository tenantRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = header.substring(PREFIX.length()).trim();
        if (!apiKey.startsWith("amk_")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String prefix = ApiKeyGenerator.extractPrefix(apiKey);
            String hash = ApiKeyGenerator.hash(apiKey);

            Optional<Tenant> tenantOpt = tenantRepository.findByApiKeyPrefixAndDeletedAtIsNull(prefix);

            if (tenantOpt.isPresent() && tenantOpt.get().getApiKeyHash().equals(hash)) {
                Tenant tenant = tenantOpt.get();
                TenantContext.set(tenant);

                var auth = new UsernamePasswordAuthenticationToken(
                        tenant.getId().toString(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_TENANT"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("🔐 Tenant autenticado: {} ({})", tenant.getName(), tenant.getId());
            } else {
                log.warn("❌ API key inválida (prefixo: {})", prefix);
            }
        } catch (Exception e) {
            log.error("Erro ao processar API key", e);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}