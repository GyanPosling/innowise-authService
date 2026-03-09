package com.innowise.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class UserServiceClientConfig {

  @Bean
  public RestClient restClient(RestClient.Builder builder) {
    return builder.build();
  }
}
