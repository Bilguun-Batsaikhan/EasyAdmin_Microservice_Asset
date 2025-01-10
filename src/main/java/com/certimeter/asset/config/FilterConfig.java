package com.certimeter.asset.config;

import com.certimeter.asset.filter.LogEndpointFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<LogEndpointFilter> loggingFilter() {
        FilterRegistrationBean<LogEndpointFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new LogEndpointFilter());
        registrationBean.addUrlPatterns("/assets/*"); // Apply filter to specific URLs
        registrationBean.setOrder(1); // Set order of the filter, lower means higher priority

        return registrationBean;
    }
}


