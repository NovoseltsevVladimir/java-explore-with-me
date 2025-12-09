package ru.practicum.ewm.event.service;

import com.querydsl.jpa.JPAExpressions;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.enums.AdminStateAction;
import ru.practicum.ewm.event.enums.EventState;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.location.mapper.LocationMapper;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.participation.ParticipationStatus;
import ru.practicum.ewm.participation.repository.ParticipationRepository;
import ru.practicum.ewm.stats.dto.StatsDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.ewm.user.service.UserService;
import ru.practicum.stats.client.StatsClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.ewm.MainServiceConstants.END_DEFAULT_DATE;
import static ru.practicum.ewm.MainServiceConstants.START_DEFAULT_DATE;
import static ru.practicum.ewm.event.model.QEvent.event;
import static ru.practicum.ewm.participation.model.QParticipationRequest.participationRequest;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final LocationRepository locationRepository;
    private final ParticipationRepository requestRepository;
    private String logInfo;
    private String appString = "/events/";
    private final StatsClient statsClient;

    //Private
    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            logInfo = "Дата начала события не может быть раньше, чем через два часа от текущего момента.";
            log.error(logInfo);
            throw new BadRequestException(logInfo);
        }

        User initiator = userService.getUserByIdWithException(userId);
        Category category = categoryService.getCategoryByIdWithException(newEventDto.getCategory());

        Location location = locationRepository.findByLatAndLon(newEventDto.getLocation().getLat(),
                        newEventDto.getLocation().getLon())
                .orElseGet(() -> locationRepository.save(LocationMapper.toLocationEntity(newEventDto.getLocation())));

        Event event = EventMapper.mapToEvent(newEventDto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        Event savedEvent = eventRepository.save(event);

        return EventMapper.mapToEventFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {

        if (!userRepository.existsById(userId)) {
            logInfo = "Пользователь с id " + userId + " не найден.";
            log.error(logInfo);
            throw new NotFoundException(logInfo);
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        List<EventShortDto> dtoList = events
                .stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());

        dtoList = fillViewsAndRequestsInShortDtoList(dtoList);

        return dtoList;
    }

    @Override
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    logInfo = "Событие с id " + eventId + " не найдено у пользователя с id " + userId;
                    log.error(logInfo);
                    return new NotFoundException(logInfo);
                });

        EventFullDto dto = EventMapper.mapToEventFullDto(event);
        List<EventFullDto> dtoList = List.of(dto);
        dtoList = fillViewsAndRequestsInFullDtoList(dtoList);
        dto = dtoList.get(0);

        return dto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    logInfo = "Событие не найдено или пользователь не является инициатором.";
                    log.error(logInfo);
                    return new NotFoundException(logInfo);
                });

        if (event.getState().equals(EventState.PUBLISHED)) {
            logInfo = "Невозможно обновить событие со статусом PUBLISHED.";
            log.error(logInfo);
            throw new ValidationException(logInfo);
        }

        if (updateRequest.getEventDate() != null
                && updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            logInfo = "Дата начала события не может быть раньше, чем через два часа от текущего момента.";
            log.error(logInfo);
            throw new BadRequestException(logInfo);
        }

        EventMapper.updateEventFromUserRequest(updateRequest, event);

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        Event updatedEvent = eventRepository.save(event);

        EventFullDto dto = EventMapper.mapToEventFullDto(updatedEvent);
        List<EventFullDto> dtoList = List.of(dto);
        dtoList = fillViewsAndRequestsInFullDtoList(dtoList);
        dto = dtoList.get(0);

        return dto;
    }

    //Public
    @Override
    public List<EventShortDto> searchEventsPublic(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime start, LocalDateTime end,
                                                  Boolean onlyAvailable, String sort,
                                                  Integer from, Integer size) {

        if (start != null && end != null && start.isAfter(end)) {
            logInfo = "Дата окончания диапазона поиска не может быть раньше даты начала.";
            log.error(logInfo);
            throw new BadRequestException(logInfo);
        }

        if (start == null) {
            start = LocalDateTime.now();
        }

        BooleanExpression searchByParameters = event.eventDate.after(start);
        if (end != null) {
            searchByParameters = searchByParameters.and(event.eventDate.before(end));
        }

        if (paid != null) {
            searchByParameters = searchByParameters.and(event.paid.eq(paid));
        }

        if (categories != null && !categories.isEmpty()) {
            searchByParameters = searchByParameters.and(event.category.id.in(categories));
        }

        if (text != null && !text.isEmpty()) {
            BooleanExpression textCondition = event.annotation.toLowerCase().likeIgnoreCase("%" + text + "%")
                    .or(event.title.toLowerCase().likeIgnoreCase("%" + text + "%"));
            searchByParameters = searchByParameters.and(textCondition);
        }

        if (Boolean.TRUE.equals(onlyAvailable)) {
            com.querydsl.core.types.Expression<Long> approvedCount = JPAExpressions
                    .select(participationRequest.id.count())
                    .from(participationRequest)
                    .where(participationRequest.event.id.eq(event.id)
                            .and(participationRequest.status.eq(ParticipationStatus.CONFIRMED)));

            searchByParameters = searchByParameters.and(event.participantLimit.gt(approvedCount));
        }

        boolean sortByViews = sort.equalsIgnoreCase("VIEWS");
        Sort springSort;
        PageRequest pageable;
        if (!sortByViews) {
            springSort = Sort.by("eventDate").ascending(); //по умолчанию сортируем по дате
            pageable = PageRequest.of(from / size, size, springSort);
        } else {
            pageable = PageRequest.of(from / size, size);
        }

        Iterable<Event> eventsIterable = eventRepository.findAll(searchByParameters, pageable);
        List<Event> events = new ArrayList<>(); // Создаем новый ArrayList
        eventsIterable.forEach(events::add);

        List<EventShortDto> eventsDto = events.stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());

        eventsDto = fillViewsAndRequestsInShortDtoList(eventsDto);

        if (sortByViews) {
            eventsDto.sort(Comparator.comparingLong(EventShortDto::getViews).reversed());
        }

        return eventsDto;

    }

    @Override
    public EventFullDto getEventByIdPublic(Long eventId) {
        logInfo = String.format("Публичный запрос события по ID: %d", eventId);
        log.info(logInfo);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    logInfo = "Событие с ID " + eventId + " не найдено.";
                    log.error(logInfo);
                    return new NotFoundException(logInfo);
                });

        if (!event.getState().equals(EventState.PUBLISHED)) {
            logInfo = "Событие с ID " + eventId + " не опубликовано и недоступно.";
            log.error(logInfo);
            throw new NotFoundException(logInfo);
        }

        EventFullDto dto = EventMapper.mapToEventFullDto(event);
        List<EventFullDto> dtoList = List.of(dto);
        dtoList = fillViewsAndRequestsInFullDtoList(dtoList);
        dto = dtoList.get(0);

        return dto;
    }

    //Admin
    @Override
    public List<EventFullDto> searchEventsByAdmin(List<Long> users, List<EventState> states,
                                                  List<Long> categories, LocalDateTime start,
                                                  LocalDateTime end, Integer from,
                                                  Integer size) {
        logInfo = String.format("Админ поиск событий: users=%s, states=%s, categories=%s",
                users, states, categories);
        log.info(logInfo);

        if (start != null && end != null && start.isAfter(end)) {
            logInfo = "Дата окончания диапазона поиска не может быть раньше даты начала.";
            log.error(logInfo);
            throw new BadRequestException(logInfo);
        }

        if (start == null) {
            start = LocalDateTime.now();
        }

        BooleanExpression searchByParameters = event.eventDate.after(start);
        if (end != null) {
            searchByParameters = searchByParameters.and(event.eventDate.before(end));
        }

        if (users != null && !users.isEmpty()) {
            searchByParameters = searchByParameters.and(event.initiator.id.in(users));
        }

        if (states != null && !states.isEmpty()) {
            searchByParameters = searchByParameters.and(event.state.in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            searchByParameters = searchByParameters.and(event.category.id.in(categories));
        }

        PageRequest pageable;

        pageable = PageRequest.of(from / size, size);

        Iterable<Event> eventsIterable = eventRepository.findAll(searchByParameters, pageable);
        List<Event> events = new ArrayList<>(); // Создаем новый ArrayList
        eventsIterable.forEach(events::add);

        List<EventFullDto> eventsDto = events.stream()
                .map(EventMapper::mapToEventFullDto)
                .collect(Collectors.toList());

        eventsDto = fillViewsAndRequestsInFullDtoList(eventsDto);

        return eventsDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        logInfo = String.format("Админ обновление события с ID %d, действие: %s",
                eventId, updateRequest.getStateAction());
        log.info(logInfo);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    logInfo = "Событие с ID " + eventId + " не найдено.";
                    log.error(logInfo);
                    throw new NotFoundException(logInfo);
                });

        LocalDateTime newDate = updateRequest.getEventDate();
        if (newDate != null && newDate.isBefore(LocalDateTime.now().plusHours(1))) {
            logInfo = "Дата начала события должна быть не ранее чем за час от текущего момента публикации.";
            log.error(logInfo);
            throw new BadRequestException(logInfo);
        }

        if (updateRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
            if (!event.getState().equals(EventState.PENDING)) {
                logInfo = "Событие не может быть опубликовано, так как его текущий статус: " + event.getState();
                log.error(logInfo);
                throw new ValidationException(logInfo);
            }
            if (updateRequest.getEventDate() != null && updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                logInfo = "Дата начала события должна быть не ранее чем за час от текущего момента публикации.";
                log.error(logInfo);
                throw new BadRequestException(logInfo);
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());

        } else if (updateRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {
            if (event.getState().equals(EventState.PUBLISHED)) {
                logInfo = "Событие не может быть отклонено, так как оно уже опубликовано.";
                log.error(logInfo);
                throw new ValidationException(logInfo);
            }
            event.setState(EventState.CANCELED);
        }

        EventMapper.updateEventFromAdminRequest(updateRequest, event);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с ID {} успешно обновлено администратором.", eventId);

        EventFullDto dto = EventMapper.mapToEventFullDto(updatedEvent);
        List<EventFullDto> dtoList = List.of(dto);
        dtoList = fillViewsAndRequestsInFullDtoList(dtoList);
        dto = dtoList.get(0);

        return dto;
    }

    private List<EventShortDto> fillViewsAndRequestsInShortDtoList(List<EventShortDto> dtoList) {

        List<String> uris = dtoList
                .stream()
                .map(dto -> appString + dto.getId())
                .collect(Collectors.toList());

        List<StatsDto> statsDto = statsClient.stats(START_DEFAULT_DATE, END_DEFAULT_DATE, uris, true);

        for (EventShortDto event : dtoList) {
            Optional<Long> optionalHits = statsDto
                    .stream()
                    .filter(currentStat -> currentStat.getUri().equals("/events/" + event.getId()))
                    .map(currentStat -> currentStat.getHits())
                    .findFirst();

            if (optionalHits.isPresent()) {
                event.setViews(optionalHits.get());
            }
        }

        dtoList = dtoList
                .stream()
                .peek(dto ->
                        dto.setConfirmedRequests(requestRepository.countByEvent_IdAndStatus(dto.getId(),
                                ParticipationStatus.CONFIRMED)))
                .collect(Collectors.toList());

        return dtoList;
    }

    private List<EventFullDto> fillViewsAndRequestsInFullDtoList(List<EventFullDto> dtoList) {

        List<String> uris = dtoList
                .stream()
                .map(dto -> appString + dto.getId())
                .collect(Collectors.toList());

        List<StatsDto> statsDto = statsClient.stats(START_DEFAULT_DATE, END_DEFAULT_DATE, uris, true);

        for (EventFullDto event : dtoList) {
            Optional<Long> optionalHits = statsDto
                    .stream()
                    .filter(currentStat -> currentStat.getUri().equals("/events/" + event.getId()))
                    .map(currentStat -> currentStat.getHits())
                    .findFirst();

            if (optionalHits.isPresent()) {
                event.setViews(optionalHits.get());
            }
        }

        dtoList = dtoList
                .stream()
                .peek(dto ->
                        dto.setConfirmedRequests(requestRepository.countByEvent_IdAndStatus(dto.getId(),
                                ParticipationStatus.CONFIRMED)))
                .collect(Collectors.toList());

        return dtoList;
    }
}