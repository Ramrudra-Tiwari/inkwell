package com.inkwell.comment.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSettings {

    @Id
    private Integer id; // Always 1

    private boolean moderationRequired;
}
