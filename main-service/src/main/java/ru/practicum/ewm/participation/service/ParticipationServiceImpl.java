package ru.practicum.ewm.participation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.enums.EventState;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.participation.ParticipationStatus;
import ru.practicum.ewm.participation.dto.ParticipationStatusDto;
import ru.practicum.ewm.participation.dto.ParticipationStatusDtoUpdateResult;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.mapper.ParticipationMapper;
import ru.practicum.ewm.participation.model.ParticipationRequest;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationServiceImpl implements ParticipationService {
    private final ParticipationRepository requestRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        User user = userService.getUserByIdWithException(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByRequester(user);
        return requests.stream()
                .map(ParticipationMapper::mapToParticipationDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        Optional<ParticipationRequest> existingRequest = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (existingRequest.isPresent()) {
            throw new ValidationException("Запрос на участие уже существует");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Инициатор не может присоединиться к своему мероприятию");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Нельзя присоединиться к неопубликованному комментарию");
        }

        if (event.getParticipantLimit() > 0) {
            Long confirmedRequests = requestRepository.countByEvent_IdAndStatus(eventId, ParticipationStatus.CONFIRMED);
            if (confirmedRequests >= event.getParticipantLimit()) {
                throw new ValidationException("В событии достигнут лимит участников");
            }
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setRequester(user);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationStatus.CONFIRMED);
        } else {
            request.setStatus(ParticipationStatus.PENDING);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        return ParticipationMapper.mapToParticipationDto(savedRequest);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найдено"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidationException("Пользователь не является владельцем запроса");
        }

        if (request.getStatus().equals(ParticipationStatus.PENDING)
                || request.getStatus().equals(ParticipationStatus.CONFIRMED)) {
            request.setStatus(ParticipationStatus.CANCELED);
        }

        return ParticipationMapper.mapToParticipationDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        // Проверяем, что пользователь является инициатором события
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found."));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("User is not the initiator of this event.");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(ParticipationMapper::mapToParticipationDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationStatusDtoUpdateResult processEventRequests(Long userId, Long eventId,
                                                                   ParticipationStatusDto requestStatusUpdate) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не удалось найти событие " + eventId));

        ParticipationStatusDtoUpdateResult result = new ParticipationStatusDtoUpdateResult();

        List<Long> requestsIds = requestStatusUpdate.getRequestIds();

        Integer participantLimit = event.getParticipantLimit();
        Long participantApproved = requestRepository.countByEvent_IdAndStatus(eventId,
                ParticipationStatus.CONFIRMED);

        if (participantApproved >= participantLimit && participantLimit != 0) {
            throw new ValidationException("Лимит участников достигнут");
        }

        List<ParticipationRequest> requests = requestRepository.findByIdIn(requestsIds);

        List<ParticipationRequestDto> approved = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();
        ;

        for (ParticipationRequest request : requests) {
            if ((participantApproved < participantLimit
                    && requestStatusUpdate.getStatus() == ParticipationStatus.CONFIRMED)
                    || participantLimit == 0 && requestStatusUpdate.getStatus() == ParticipationStatus.PENDING) {
                request.setStatus(requestStatusUpdate.getStatus());
                ParticipationRequest updatedRequest = requestRepository.save(request);
                approved.add(ParticipationMapper.mapToParticipationDto(updatedRequest));
                participantApproved += 1;
            } else {
                request.setStatus(ParticipationStatus.REJECTED);
                ParticipationRequest updatedRequest = requestRepository.save(request);
                rejected.add(ParticipationMapper.mapToParticipationDto(updatedRequest));
            }
        }

        result.setConfirmedRequests(approved);
        result.setRejectedRequests(rejected);

        return result;
    }


}

