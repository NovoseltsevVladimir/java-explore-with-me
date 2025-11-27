package ru.practicum.ewm.stats.service.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.dto.HitDto;
import ru.practicum.ewm.stats.service.model.Hit;

import java.time.LocalDateTime;

@Component
public class HitMapper {

    public static Hit mapToHit(HitDto request) {

        Hit hit = new Hit();
        hit.setUri(request.getUri());
        hit.setApp(request.getApp());
        hit.setIp(request.getIp());
        hit.setTimestamp(LocalDateTime.now());

        return hit;
    }

    public static HitDto mapToHitDto(Hit hit) {

        HitDto dto = new HitDto();
        dto.setId(hit.getId());
        dto.setUri(hit.getUri());
        dto.setApp(hit.getApp());
        dto.setIp(hit.getIp());
        dto.setTimestamp(hit.getTimestamp());

        return dto;
    }
}
