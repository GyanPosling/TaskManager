package com.bsuir.taskmanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    @Schema(description = "Comment id", example = "12")
    private Long id;

    @Schema(description = "Comment text", example = "Please update the estimates")
    private String text;

    @Schema(description = "Creation time", example = "2026-02-12T09:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Task id", example = "10")
    private Long taskId;

    @Schema(description = "Author user id", example = "3")
    private Long authorId;
}
