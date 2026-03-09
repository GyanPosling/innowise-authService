package com.innowise.authservice.client;

import com.innowise.authservice.model.dto.request.CreateUserProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class UserServiceClient {

  private static final String INTERNAL_SECRET_HEADER = "X-INTERNAL-SECRET";

  private final RestClient restClient;

  @Value("${services.userservice-url}")
  private String userServiceUrl;

  @Value("${services.internal-secret}")
  private String internalSecret;

  public void createUserProfile(CreateUserProfileRequest request) {
    restClient.post()
        .uri(userServiceUrl + "/api/users/internal")
        .headers(headers -> headers.addAll(createHeaders()))
        .body(request)
        .retrieve()
        .toBodilessEntity();
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(INTERNAL_SECRET_HEADER, internalSecret);
    return headers;
  }
}
