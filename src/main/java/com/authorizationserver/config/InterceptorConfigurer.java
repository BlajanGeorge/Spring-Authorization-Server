package com.authorizationserver.config;

import com.authorizationserver.model.MetadataContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.authorizationserver.constants.Constants.GET_METADATA_PATH;

/**
 * Configuration class for auth service interceptors.
 *
 * @author Blajan George
 */
@Configuration
public class InterceptorConfigurer implements WebMvcConfigurer {
    /**
     * Metadata context model object
     */
    private final MetadataContext metadataContext;

    public InterceptorConfigurer(MetadataContext metadataContext) {
        this.metadataContext = metadataContext;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MetadataContextInterceptor(metadataContext))
                .addPathPatterns(GET_METADATA_PATH);
    }
}
