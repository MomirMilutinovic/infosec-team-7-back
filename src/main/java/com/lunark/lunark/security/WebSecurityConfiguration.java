package com.lunark.lunark.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration {

    private final TokenEntryPoint tokenEntryPoint;
    private final MvcRequestMatcher.Builder mvcMatcherBuilder;
    private final KeycloakJwtAuthConverter keycloakJwtAuthConverter;

    @Autowired
    @Lazy
    public WebSecurityConfiguration(KeycloakJwtAuthConverter keycloakJwtAuthConverter, TokenEntryPoint tokenEntryPoint, HandlerMappingIntrospector introspector) {
        this.keycloakJwtAuthConverter = keycloakJwtAuthConverter;
        this.tokenEntryPoint = tokenEntryPoint;
        mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    public WebMvcConfigurer CORSConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://localhost:4200")
                        .allowedHeaders("*")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")
                        .allowCredentials(false);
            }
        };
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.securityContext((securityContext) -> securityContext
                .securityContextRepository(new RequestAttributeSecurityContextRepository())
        );
        http.headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'"))
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
        );
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(tokenEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(mvcMatcherBuilder.pattern("/api/auth/**")).permitAll()
                            .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
                            .requestMatchers(mvcMatcherBuilder.pattern("/api/test/**")).permitAll()
                            .requestMatchers(mvcMatcherBuilder.pattern("/api/**")).permitAll()
                .anyRequest().authenticated()
                );
        http.oauth2ResourceServer((oauth2) -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthConverter)));
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder encodePassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        MvcRequestMatcher loginMatcher = mvcMatcherBuilder.pattern("/api/auth/login");
        loginMatcher.setMethod(HttpMethod.POST);
        MvcRequestMatcher signupMatcher = mvcMatcherBuilder.pattern("/api/auth/signup");
        loginMatcher.setMethod(HttpMethod.POST);
    	return (web) -> web.ignoring().requestMatchers(loginMatcher).requestMatchers(signupMatcher)
                .requestMatchers(new AntPathRequestMatcher("/"))
                .requestMatchers(new AntPathRequestMatcher("/webjars/**"))
                .requestMatchers(new AntPathRequestMatcher("/*.html"))
                .requestMatchers(new AntPathRequestMatcher("favicon.ico"))
                .requestMatchers(new AntPathRequestMatcher("/**/*.html"))
                .requestMatchers(new AntPathRequestMatcher("/**/*.css"))
                .requestMatchers(new AntPathRequestMatcher("/**/*.js"))
                .requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.OPTIONS, "/**"));

    }

    @Bean
    public FilterRegistrationBean<XSSFilter> filterRegistrationBean() {
        FilterRegistrationBean<XSSFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XSSFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}