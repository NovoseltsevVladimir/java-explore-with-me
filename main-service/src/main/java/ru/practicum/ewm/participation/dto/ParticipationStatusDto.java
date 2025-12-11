package ru.practicum.ewm.participation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.participation.ParticipationStatus;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ParticipationStatusDto {
    private List<Long> requestIds;
    private ParticipationStatus status;
}
