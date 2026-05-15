//package com.inkwell.auth.filter;
//
//import com.inkwell.auth.service.JwtUtils;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Collections;
//
///**
// * JWT Authentication Filter for validating tokens.
// */
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtUtils jwtUtils;
//
//    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
//        this.jwtUtils = jwtUtils;
//    }
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        String path = request.getServletPath();
//
//        // Public routes
//        if (path.startsWith("/auth") ||
//                path.startsWith("/v3/api-docs") ||
//                path.startsWith("/swagger-ui")) {
//
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String authHeader = request.getHeader("Authorization");
//
//        // Token missing
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Token Missing");
//            return;
//        }
//
//        String token = authHeader.substring(7);
//
//        try {
//            String email = jwtUtils.extractEmail(token);
//            String role = jwtUtils.extractRole(token);
//
//            if (!jwtUtils.validateToken(token)) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("Invalid Token");
//                return;
//            }
//
//            UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(
//                            email,
//                            null,
//                            Collections.singletonList(
//                                    new SimpleGrantedAuthority("ROLE_" + role)
//                            )
//                    );
//
//            SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        } catch (Exception e) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("Invalid Token");
//            return;
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}
