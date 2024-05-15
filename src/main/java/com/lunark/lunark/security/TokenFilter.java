package com.lunark.lunark.security;

import com.lunark.lunark.util.TokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenFilter.class);
    private final UserDetailsService userDetailsService;
    private final TokenUtils tokenUtil;

    public TokenFilter(TokenUtils tokenUtil, UserDetailsService userDetailsService) {
        this.tokenUtil = tokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    public TokenBasedAuth setAuthentication(String authToken) {
        String username;
        try {
            if (authToken != null) {
                username = tokenUtil.getUsernameFromToken(authToken);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (!userDetails.isAccountNonLocked()) {
                        throw new LockedException("Account is locked");
                    }
                    if (tokenUtil.validateToken(authToken, userDetails)) {
                        TokenBasedAuth authentication = new TokenBasedAuth(userDetails);
                        authentication.setToken(authToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        return authentication;
                    }
                }
            }
        }
        catch (ExpiredJwtException ex) {
            logger.debug("Token expired!");
        }
        catch (SignatureException ex) {
            logger.debug("Unsigned token passed");
        }
        return null;
    }
}