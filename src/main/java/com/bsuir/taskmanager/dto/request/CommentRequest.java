package com.bsuir.taskmanager.dto.request;

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
public class CommentRequest {
    @Schema(description = "Comment text", example = "Please update the estimates")
    @NotBlank
    @Size(max = 2000)
    private String text;

    @Schema(description = "Task id", example = "10")
    @NotNull
    @Positive
    private Long taskId;

    @Schema(description = "Author user id", example = "3")
    @NotNull
    @Positive
    private Long authorId;
}
