package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.comments.dto.CommentDto;
import ru.practicum.ewm.event.comments.dto.NewCommentDto;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    //Private
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size);

    EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    //Admin
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    List<EventFullDto> searchEventsByAdmin(List<Long> users,
                                           List<EventState> states,
                                           List<Long> categories,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           Integer from,
                                           Integer size);


    //Public

    List<EventShortDto> searchEventsPublic(String text,
                                           List<Long> categories,
                                           Boolean paid,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           Boolean onlyAvailable,
                                           String sort,
                                           Integer from,
                                           Integer size);

    EventFullDto getEventByIdPublic(Long eventId);

    CommentDto createComment(NewCommentDto newComment);
}
