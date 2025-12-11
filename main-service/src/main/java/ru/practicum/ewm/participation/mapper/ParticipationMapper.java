package ru.practicum.ewm.participation.mapper;

import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.model.ParticipationRequest;

public class ParticipationMapper {

    public static ParticipationRequestDto mapToParticipationDto(ParticipationRequest request) {

        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setCreated(request.getCreated());
        dto.setEvent(request.getEvent().getId());
        dto.setRequester(request.getRequester().getId());
        dto.setStatus(request.getStatus());

        return dto;
    }
}

