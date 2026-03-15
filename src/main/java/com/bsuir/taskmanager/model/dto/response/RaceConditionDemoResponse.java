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
public class RaceConditionDemoResponse {
    @Schema(description = "Number of parallel threads used", example = "50")
    private int threadCount;

    @Schema(description = "Increments per thread", example = "1000")
    private int incrementsPerThread;

    @Schema(description = "Unsafe counter demonstration result")
    private RaceConditionCounterResultResponse unsafeCounter;

    @Schema(description = "Synchronized counter demonstration result")
    private RaceConditionCounterResultResponse synchronizedCounter;

    @Schema(description = "Atomic counter demonstration result")
    private RaceConditionCounterResultResponse atomicCounter;
}
