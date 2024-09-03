package com.example.demo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.demo.DemoAppConstants.ALLOWED_ORIGIN;
import static com.example.demo.DemoAppConstants.AUTHENTICATED_ENDPOINT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {DemoApplication.class, CorsWithCrossOriginAndHttpCorsTest.SecurityConfigWithHttpCors.class})
public class CorsWithCrossOriginAndHttpCorsTest extends CorsAbstractTest {

    @TestConfiguration
    public static class SecurityConfigWithHttpCors {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests((requests) -> requests.anyRequest().authenticated())
                    .httpBasic(withDefaults());

            http.cors(withDefaults());

            return http.build();
        }

        @RestController
        @CrossOrigin(origins = ALLOWED_ORIGIN)
        public class CorsController {

            @GetMapping(AUTHENTICATED_ENDPOINT)
            public String authenticated() {
                return "OK";
            }
        }
    }
}
