package com.clinc_cms.management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfigProperties {
    private String secret;
    private long expiration;
}
