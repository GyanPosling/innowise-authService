package com.innowise.authservice.client;

import com.innowise.authservice.model.dto.request.InternalUserAuthLinkRequest;
import com.innowise.authservice.model.dto.request.InternalUserCreateRequest;
import com.innowise.authservice.model.dto.response.InternalUserCreateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UserServiceClient {

  private static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";

  private final WebClient webClient;
  private final String baseUrl;
  private final String internalSecret;

  public UserServiceClient(
      WebClient.Builder webClientBuilder,
      @Value("${userservice.base-url}") String baseUrl,
      @Value("${authservice.internal-endpoint-secret}") String internalSecret) {
    this.webClient = webClientBuilder.build();
    this.baseUrl = baseUrl;
    this.internalSecret = internalSecret;
  }

  public Integer createInternalUser(InternalUserCreateRequest request) {
    InternalUserCreateResponse response = webClient.post()
        .uri(baseUrl + "/api/internal/users")
        .header(INTERNAL_SECRET_HEADER, internalSecret)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(InternalUserCreateResponse.class)
        .block();
    if (response == null) {
      return null;
    }
    return response.getUserId();
  }

  public void linkAuthUser(Integer userId, Long authUserId) {
    webClient.patch()
        .uri(baseUrl + "/api/internal/users/{userId}/auth", userId)
        .header(INTERNAL_SECRET_HEADER, internalSecret)
        .bodyValue(InternalUserAuthLinkRequest.builder().authUserId(authUserId).build())
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  public void rollbackUser(Integer userId) {
    webClient.delete()
        .uri(baseUrl + "/api/internal/users/{userId}", userId)
        .header(INTERNAL_SECRET_HEADER, internalSecret)
        .retrieve()
        .toBodilessEntity()
        .block();
  }
}
