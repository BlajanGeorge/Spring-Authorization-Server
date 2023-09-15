package com.authorizationserver.config;

import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.Writer;

import static com.authorizationserver.constants.Constants.GET_JWK_SET_PATH;

/**
 * Custom filter for JWK Set endpoint
 *
 * @author Blajan George
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JWKSetEndpointFilter extends OncePerRequestFilter {
    /**
     * JWK Source
     */
    private final CustomJWKSource jwkSource;
    /**
     * JWK Selector
     */
    private final JWKSelector jwkSelector;
    /**
     * Request matcher
     */
    private final RequestMatcher requestMatcher;

    public JWKSetEndpointFilter(CustomJWKSource jwkSource) {
        this.jwkSource = jwkSource;
        this.jwkSelector = new JWKSelector(new JWKMatcher.Builder().build());
        this.requestMatcher = new AntPathRequestMatcher(GET_JWK_SET_PATH, HttpMethod.GET.name());
    }

    /**
     * @param request     request
     * @param response    response
     * @param filterChain filter chain
     * @throws ServletException Servlet exception
     * @throws IOException      IOException
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!this.requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        JWKSet jwkSet;
        try {
            jwkSet = new JWKSet(this.jwkSource.getLastNPublicKeys(this.jwkSelector));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to select the JWK(s) -> " + ex.getMessage(), ex);
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try (Writer writer = response.getWriter()) {
            writer.write(jwkSet.toString());    // toString() excludes private keys
        }
    }
}
