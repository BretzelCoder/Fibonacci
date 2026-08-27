package com.bretzelcoder.fibonacci.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Opens the JSON API to the Vite dev server origin, so the Vue frontend living in
 * {@code fibonacci-dotnet/frontend} can be pointed at this stack unchanged.
 * Only {@code /api/**} is exposed; the Thymeleaf pages are same-origin by nature.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String VITE_DEV_ORIGIN = "http://localhost:5173";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(VITE_DEV_ORIGIN)
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
