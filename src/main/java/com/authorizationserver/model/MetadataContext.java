package com.authorizationserver.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Customer context model object
 *
 * @author Blajan George
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Component
@RequestScope
public class MetadataContext {
    private String issuer;
}
