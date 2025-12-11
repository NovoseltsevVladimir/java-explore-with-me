package ru.practicum.ewm.participation.service;

import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.dto.ParticipationStatusDto;
import ru.practicum.ewm.participation.dto.ParticipationStatusDtoUpdateResult;

import java.util.List;

public interface ParticipationService {

    List<ParticipationRequestDto> getRequestsByUserId(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    ParticipationStatusDtoUpdateResult processEventRequests(Long userId, Long eventId, ParticipationStatusDto requestStatusUpdate);
}

