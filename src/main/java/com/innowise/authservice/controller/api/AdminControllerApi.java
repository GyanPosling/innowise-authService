package com.innowise.authservice.controller.api;

import com.innowise.authservice.model.dto.response.PromoteUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin", description = "Admin user management API")
public interface AdminControllerApi {

    @Operation(summary = "Promote user to ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User promoted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    ResponseEntity<PromoteUserResponse> promoteToAdmin(UUID id);
}
