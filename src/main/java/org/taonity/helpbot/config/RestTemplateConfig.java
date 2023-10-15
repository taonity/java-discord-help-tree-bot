package org.taonity.helpbot.config;

import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        final var loggingInterceptorLogger = LoggerFactory.getLogger(LoggingInterceptor.class);
        final RestTemplate restTemplate;
        if (loggingInterceptorLogger.isDebugEnabled()) {
            final var factory = new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory());
            restTemplate = new RestTemplate(factory);
            var interceptors = restTemplate.getInterceptors();
            if (CollectionUtils.isEmpty(interceptors)) {
                interceptors = new ArrayList<>();
            }
            interceptors.add(new LoggingInterceptor());
            restTemplate.setInterceptors(interceptors);
        } else {
            restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        }

        return restTemplate;
    }
}
