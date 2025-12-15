package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class NewCommentDto {

    @NotBlank(message = "Текст комментария не может быть пустым или состоять только из пробелов.")
    @Size(min = 1, max = 255, message = "Длина текста должна быть от 1 до 255 символов.")
    private String text;
    private Long eventId;
    private Long authorId;
    private LocalDateTime created;
}
