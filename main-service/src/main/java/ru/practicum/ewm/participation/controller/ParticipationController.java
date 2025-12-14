package ru.practicum.ewm.participation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.participation.dto.ParticipationRequestDto;
import ru.practicum.ewm.participation.dto.ParticipationStatusDto;
import ru.practicum.ewm.participation.dto.ParticipationStatusDtoUpdateResult;
import ru.practicum.ewm.participation.service.ParticipationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class ParticipationController {

    private final ParticipationService participationService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        return participationService.createRequest(userId, eventId);
    }

    @GetMapping("/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsForUser(@PathVariable Long userId) {
        return participationService.getRequestsByUserId(userId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return participationService.cancelRequest(userId, requestId);
    }

    @GetMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventRequestsForInitiator(@PathVariable Long userId, @PathVariable Long eventId) {
        return participationService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationStatusDtoUpdateResult processEventRequests(@PathVariable Long userId, @PathVariable Long eventId,
                                                                   @RequestBody ParticipationStatusDto requestStatusUpdate) {
        return participationService.processEventRequests(userId, eventId, requestStatusUpdate);
    }
}

