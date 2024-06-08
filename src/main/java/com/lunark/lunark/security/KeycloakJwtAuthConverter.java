package com.lunark.lunark.security;

import com.lunark.lunark.util.TokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KeycloakJwtAuthConverter implements Converter<Jwt, TokenBasedAuth> {
    private static final String GROUPS = "groups";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final Logger logger = LoggerFactory.getLogger(KeycloakJwtAuthConverter.class);
    private final UserDetailsService userDetailsService;
    private final TokenUtils tokenUtils;

    public KeycloakJwtAuthConverter(UserDetailsService userDetailsService, TokenUtils tokenUtils) {
        this.userDetailsService = userDetailsService;
        this.tokenUtils = tokenUtils;
    }

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public TokenBasedAuth convert(@NonNull Jwt jwt) {
        return getAuthentication(jwt.getTokenValue());
    }

    public TokenBasedAuth getAuthentication(String authToken) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) tokenUtils.getPublicKey()).signatureAlgorithm(SignatureAlgorithm.RS256).build();
        Jwt jwt = jwtDecoder.decode(authToken);
        Collection<GrantedAuthority> permissions =
                Stream.concat(
                                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                                extractResourceRoles(jwt).stream()
                        )
                        .collect(Collectors.toSet());
        String username;
        try {
            if (authToken != null) {
                username = tokenUtils.getUsernameFromToken(authToken);
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (!userDetails.isAccountNonLocked()) {
                        throw new LockedException("Account is locked");
                    }
                    if (tokenUtils.validateToken(authToken, userDetails)) {
                        TokenBasedAuth authentication = new TokenBasedAuth(userDetails, permissions);
                        authentication.setToken(authToken);
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

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Collection<String> roles = new ArrayList<>();

        if (jwt.hasClaim(REALM_ACCESS_CLAIM)) {
            var realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
            roles = (Collection<String>) realmAccess.get(ROLES_CLAIM);
        } else if (jwt.hasClaim(GROUPS)) {
            roles = jwt.getClaim(
                    GROUPS);
        }

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public <U> Converter<Jwt, U> andThen(Converter<? super TokenBasedAuth, ? extends U> after) {
        return Converter.super.andThen(after);
    }
}
