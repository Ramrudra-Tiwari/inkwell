package com.inkwell.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotNull(message = "Post ID is required")
    private Integer postId;

    @NotNull(message = "Author ID is required")
    private Integer authorId;

    private Integer parentCommentId;

    @NotBlank(message = "Content cannot be blank")
    private String content;
}
