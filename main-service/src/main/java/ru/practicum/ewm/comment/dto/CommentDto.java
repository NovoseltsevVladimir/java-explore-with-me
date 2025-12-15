package ru.practicum.ewm.comment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private Long eventId;
    private String authorName;
    private LocalDateTime created;
}
