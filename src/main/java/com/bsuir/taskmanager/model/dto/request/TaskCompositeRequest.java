package com.bsuir.taskmanager.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompositeRequest extends TaskRequest {
    @Schema(description = "New tag name", example = "urgent")
    @NotBlank
    @Size(max = 80)
    private String tagName;

    @Schema(description = "Comment text", example = "Please update the estimates")
    @NotBlank
    @Size(max = 2000)
    private String commentText;

    @Schema(description = "Comment author user id", example = "3")
    @NotNull
    @Positive
    private Long commentAuthorId;

    @Schema(description = "Force an error after task and tag are saved", example = "true")
    private boolean failAfterTask;
}
