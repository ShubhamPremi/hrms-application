package com.hrms.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter guarantees this filter runs exactly once per request
    // Spring's filter chain can sometimes call filters multiple times in
    // forwarded/included requests — OncePerRequestFilter prevents that

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Read Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If no Bearer token, skip this filter and continue the chain
        // The AuthorizationFilter will then reject the request if the endpoint
        // requires authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Token is malformed or signature is invalid
            log.warn("Invalid JWT token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Step 4: Only authenticate if we have a username AND no auth exists yet
        // SecurityContextHolder.getContext().getAuthentication() is not null
        // if the user was already authenticated earlier in the chain
        if (userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 5: Load full user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Step 6: Validate token against the loaded user
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Step 7: Create authentication token
                // This is the object Spring Security uses to represent an
                // authenticated principal for the rest of this request
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credentials null after auth
                                userDetails.getAuthorities()   // roles
                        );

                // Attach request details (IP, session) to the auth token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 8: Store in SecurityContextHolder
                // From this point forward, any code in this request can call:
                // SecurityContextHolder.getContext().getAuthentication()
                // and get this authenticated user
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("JWT authenticated user: {} with role: {}",
                        userEmail, userDetails.getAuthorities());
            }
        }

        // Step 9: Continue the filter chain regardless
        // If authentication failed, SecurityContextHolder remains empty
        // and AuthorizationFilter will reject the request
        filterChain.doFilter(request, response);
    }
}