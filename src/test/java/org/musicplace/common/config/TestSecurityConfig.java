package org.musicplace.common.config;

import org.musicplace.global.logging.LoggingMdcFilter;
import org.musicplace.global.security.jwt.JwtAuthenticationFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return mock(JwtAuthenticationFilter.class);
    }

    @Bean
    public LoggingMdcFilter loggingMdcFilter() {
        return mock(LoggingMdcFilter.class);
    }
}
