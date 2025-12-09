package ru.practicum.ewm.participation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.participation.ParticipationStatus;

import java.time.LocalDateTime;

import static ru.practicum.ewm.MainServiceConstants.DATA_DTO_PATTERN;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATA_DTO_PATTERN)
    private LocalDateTime created;

    private Long event;

    private Long id;

    private Long requester;

    private ParticipationStatus status;
}
