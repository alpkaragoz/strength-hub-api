package com.strengthhub.strength_hub_api.security;

import com.strengthhub.strength_hub_api.dto.response.ErrorResponse;
import com.strengthhub.strength_hub_api.exception.auth.AuthenticationFailedException;
import com.strengthhub.strength_hub_api.exception.common.ForbiddenAccessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            jwtUtil.validateToken(jwt);
            // Only process access tokens (not refresh tokens)
            if (!jwtUtil.isAccessToken(jwt)) {
                log.warn("Refresh token used for API access - rejecting");
                filterChain.doFilter(request, response);
                return;
            }

            // Extract user info from token claims
            String username = jwtUtil.getUsernameFromToken(jwt);
            UUID userId = jwtUtil.getUserIdFromToken(jwt);
            boolean isAdmin = jwtUtil.isAdminFromToken(jwt);
            boolean isCoach = jwtUtil.isCoachFromToken(jwt);
            boolean isLifter = jwtUtil.isLifterFromToken(jwt);

            // Build authorities from token claims
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (isAdmin) authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            if (isCoach) authorities.add(new SimpleGrantedAuthority("ROLE_COACH"));
            if (isLifter) authorities.add(new SimpleGrantedAuthority("ROLE_LIFTER"));

            // Create user principal
            UserPrincipal userPrincipal = new UserPrincipal(userId, username, authorities);

            // Set authentication in SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Set authentication for user: {} with roles: {}", username, authorities);

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Skip filter for public endpoints
        return  path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/refresh") ||
                path.equals("/api/v1/auth/register") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/actuator/health");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw AuthenticationFailedException.missingCredentials();
    }
}