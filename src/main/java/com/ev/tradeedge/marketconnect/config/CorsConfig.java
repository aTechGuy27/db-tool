package com.ev.tradeedge.marketconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow credentials
        config.setAllowCredentials(true);
        
        // Use allowedOriginPatterns instead of allowedOrigins
        config.addAllowedOriginPattern("*");
        
        // Allow common HTTP methods
        config.addAllowedMethod("*");
        
        // Allow common headers
        config.addAllowedHeader("*");
        
        // Allow exposing headers
        config.addExposedHeader("Authorization");
        
        // Apply this configuration to all paths
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
