package com.bsuir.taskmanager.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RaceConditionCounterResultResponse {
    @Schema(description = "Counter name", example = "Unsafe counter")
    private String counterName;

    @Schema(description = "Expected final value", example = "50000")
    private int expectedValue;

    @Schema(description = "Actual final value", example = "43782")
    private int actualValue;

    @Schema(description = "Number of lost updates", example = "6218")
    private int lostUpdates;

    @Schema(description = "Whether the result is correct", example = "false")
    private boolean correct;
}
