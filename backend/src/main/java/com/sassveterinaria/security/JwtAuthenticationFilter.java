package com.sassveterinaria.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ProblemResponseWriter problemResponseWriter;

    public JwtAuthenticationFilter(JwtService jwtService, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.problemResponseWriter = new ProblemResponseWriter(objectMapper);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.parse(token);

            UUID userId = UUID.fromString(claims.getSubject());
            String roleCode = String.valueOf(claims.get("role"));
            UUID branchId = UUID.fromString(String.valueOf(claims.get("branch_id")));
            String username = String.valueOf(claims.get("username"));
            String fullName = String.valueOf(claims.get("full_name"));
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) claims.get("perms", List.class);

            AuthPrincipal principal = new AuthPrincipal(userId, username, fullName, roleCode, branchId, permissions);
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            problemResponseWriter.write(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "https://sassveterinaria.local/errors/invalid-token",
                "Unauthorized",
                "Token invalido o expirado.",
                "INVALID_TOKEN"
            );
        }
    }
}
