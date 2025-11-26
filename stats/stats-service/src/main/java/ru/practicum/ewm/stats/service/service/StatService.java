package ru.practicum.service.service;

import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    HitDto createHit (HitDto hitDto);
    List<StatsDto> getStats (LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

}
