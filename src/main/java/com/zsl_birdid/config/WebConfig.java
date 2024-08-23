package com.zsl_birdid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class for setting up CORS (Cross-Origin Resource Sharing) policies.
 * This class implements WebMvcConfigurer to customize web-related configurations.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Logger for the WebConfig class
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    // Injects the server IP from application properties with a default value of "localhost"
    @Value("${SERVER_IP:localhost}")
    private String serverIp;

    // Injects the server port from application properties with a default value of "8080"
    @Value("${SERVER_PORT:8080}")
    private String serverPort;

    /**
     * Configures CORS (Cross-Origin Resource Sharing) mappings.
     * Allows requests from specified origins and supports various HTTP methods.
     *
     * @param registry  CorsRegistry object to add CORS mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Constructs allowed origin URL using injected server IP and port
        String allowedOrigin = "http://" + serverIp + ":" + serverPort;
        // Constructs local origin URL for localhost
        String localOrigin = "http://localhost:" + serverPort;

        // Logs the CORS configuration information
        logger.info("Configuring CORS with allowed origin: {}", allowedOrigin);

        // Configures CORS settings
        registry.addMapping("/**")
                .allowedOrigins(localOrigin, allowedOrigin) // Allows requests from the specified origins
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allows specified HTTP methods
                .allowCredentials(true) // Allows credentials such as cookies
                .allowedHeaders("*"); // Allows all headers
    }
}
