package com.bsuir.taskmanager.controller.api;

import com.bsuir.taskmanager.model.dto.response.RaceConditionDemoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Demo", description = "Demo endpoints for lab requirements")
@UnauthorizedApiResponse
public interface DemoControllerApi {
    @Operation(summary = "Run race condition demo")
    @ApiResponse(responseCode = "200", description = "Race condition demo completed")
    @InternalServerErrorApiResponse
    ResponseEntity<RaceConditionDemoResponse> runRaceConditionDemo() throws InterruptedException;
}
