package ru.practicum.ewm.stats.service.service;

import ru.practicum.ewm.stats.dto.HitDto;
import ru.practicum.ewm.stats.dto.StatsDto;
import ru.practicum.ewm.stats.dto.StatsDtoById;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public interface StatService {

    HitDto createHit(HitDto hitDto);

    List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);

    StatsDtoById getStatsById (List<Long> ids, String basicAdress);
}
