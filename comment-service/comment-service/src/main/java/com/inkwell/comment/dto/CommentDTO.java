package com.inkwell.comment.dto;

import com.inkwell.comment.entity.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private Integer commentId;
    private Integer postId;
    private Integer authorId;
    private Integer parentCommentId;
    private String content;
    private Integer likesCount;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
