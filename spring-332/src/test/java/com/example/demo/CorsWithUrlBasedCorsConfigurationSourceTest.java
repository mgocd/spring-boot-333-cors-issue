package com.example.demo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.example.demo.DemoAppConstants.ALLOWED_ORIGIN;
import static com.example.demo.DemoAppConstants.AUTHENTICATED_ENDPOINT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {DemoApplication.class, CorsWithUrlBasedCorsConfigurationSourceTest.SecurityConfigWithHttpCors.class})
public class CorsWithUrlBasedCorsConfigurationSourceTest extends CorsAbstractTest {

    @TestConfiguration
    public static class SecurityConfigWithHttpCors {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests((requests) -> requests.anyRequest().authenticated())
                    .httpBasic(withDefaults());

            return http.build();
        }

        @Bean
        UrlBasedCorsConfigurationSource corsConfigurationSource() {
            var configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(List.of(ALLOWED_ORIGIN));
            configuration.setAllowedMethods(List.of("GET"));
            var source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration(AUTHENTICATED_ENDPOINT, configuration);
            return source;
    }

        }


    }
