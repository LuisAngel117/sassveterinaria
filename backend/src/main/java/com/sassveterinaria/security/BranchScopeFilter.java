package com.sassveterinaria.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BranchScopeFilter extends OncePerRequestFilter {

    private final ProblemResponseWriter problemResponseWriter;

    public BranchScopeFilter(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.problemResponseWriter = new ProblemResponseWriter(objectMapper);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        if (!isBranchScopedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        Object principalObject = authentication.getPrincipal();
        if (!(principalObject instanceof AuthPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        String headerValue = request.getHeader("X-Branch-Id");
        if (headerValue == null || headerValue.isBlank()) {
            problemResponseWriter.write(
                request,
                response,
                HttpStatus.BAD_REQUEST,
                "https://sassveterinaria.local/errors/missing-branch-header",
                "Missing branch header",
                "Falta el header X-Branch-Id.",
                "MISSING_BRANCH_HEADER"
            );
            return;
        }

        UUID requestBranchId;
        try {
            requestBranchId = UUID.fromString(headerValue);
        } catch (IllegalArgumentException ex) {
            problemResponseWriter.write(
                request,
                response,
                HttpStatus.BAD_REQUEST,
                "https://sassveterinaria.local/errors/invalid-branch-header",
                "Invalid branch header",
                "X-Branch-Id no tiene formato UUID valido.",
                "INVALID_BRANCH_HEADER"
            );
            return;
        }

        if (!requestBranchId.equals(principal.getBranchId())) {
            problemResponseWriter.write(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "https://sassveterinaria.local/errors/branch-scope-mismatch",
                "Branch scope mismatch",
                "X-Branch-Id no coincide con el branch_id del token.",
                "BRANCH_SCOPE_MISMATCH"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBranchScopedPath(String path) {
        return path.startsWith("/api/v1/") && !path.startsWith("/api/v1/auth/");
    }
}
