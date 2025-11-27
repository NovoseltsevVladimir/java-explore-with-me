package ru.practicum.ewm.stats.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.dto.HitDto;
import ru.practicum.ewm.stats.dto.StatsDto;
import ru.practicum.ewm.stats.service.mapper.HitMapper;
import ru.practicum.ewm.stats.service.model.Hit;
import ru.practicum.ewm.stats.service.repository.StatServiceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatServiceImpl implements StatService {

    StatServiceRepository repository;

    @Autowired
    public StatServiceImpl(StatServiceRepository repository) {
        this.repository = repository;
    }

    @Override
    public HitDto createHit(HitDto hitDto) {

        Hit newHit = repository.save(HitMapper.mapToHit(hitDto));

        return HitMapper.mapToHitDto(newHit);
    }

    @Override
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return repository.getUniqueStatsByDates(start, end);
            } else {
                return repository.getNotUniqueStatsByDates(start, end);
            }
        } else {
            if (unique) {
                return repository.getUniqueStatsByDatesAndUris(start, end, uris);
            } else {
                return repository.getNotUniqueStatsByDatesAndUris(start, end, uris);
            }
        }
    }
}
