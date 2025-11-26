package ru.practicum.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.service.mapper.HitMapper;
import ru.practicum.service.model.Hit;
import ru.practicum.service.repository.StatServiceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatServiceImpl implements StatService{

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
        return null;
    }
}
