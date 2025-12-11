package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.location.dto.LocationDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.MainServiceConstants.DATA_DTO_PATTERN;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class UpdateEventRequest {
    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    protected String annotation;

    protected Long category;

    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
    protected String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_DTO_PATTERN)
    protected LocalDateTime eventDate;

    protected LocationDto location;

    protected Boolean paid;

    @Positive
    protected Integer participantLimit;

    protected Boolean requestModeration;

    @Size(min = 3, max = 120, message = "Заголовок должен быть от 3 до 120 символов")
    protected String title;
}
