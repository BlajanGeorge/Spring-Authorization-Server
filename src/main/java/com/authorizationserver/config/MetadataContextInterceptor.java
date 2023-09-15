package com.authorizationserver.config;

import com.authorizationserver.model.MetadataContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Interceptor to populate {@link MetadataContextInterceptor} request scoped bean.
 *
 * @author Blajan George
 */
public class MetadataContextInterceptor implements HandlerInterceptor {
    /**
     * Metadata context model object
     */
    private final MetadataContext metadataContext;

    public MetadataContextInterceptor(MetadataContext metadataContext) {
        this.metadataContext = metadataContext;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        metadataContext.setIssuer(UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(request))
                .replacePath(request.getContextPath())
                .replaceQuery(null)
                .fragment(null)
                .build()
                .toUriString());
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
