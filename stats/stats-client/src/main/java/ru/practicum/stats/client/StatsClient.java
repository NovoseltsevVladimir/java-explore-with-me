package ru.practicum.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.client.exception.ValidationException;
import ru.practicum.ewm.stats.dto.HitDto;
import ru.practicum.ewm.stats.dto.StatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {
    final RestClient restClient;
    final String statUrl;

    public StatsClient(RestClient restClient, @Value("http://localhost:9090") String statUrl) {
        this.restClient = restClient;
        this.statUrl = statUrl;
    }

    public void createHit(HitDto hitDto) {
        restClient.post().uri("/hit")
                .body(hitDto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<StatsDto> stats(LocalDateTime start, LocalDateTime end,
                                List<String> uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (start == null || end == null) {
            throw new ValidationException("Не заданы даты начала и/или окончания отбора");
        }

        if (start.isBefore(end)) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromPath("/stats")
                .queryParam("start", start.format(formatter))
                .queryParam("end", end.format(formatter));

        if (uris != null && !uris.isEmpty()) {
            uriComponentsBuilder.queryParam("uris", uris);
        }

        if (unique == null) {
            uriComponentsBuilder.queryParam("unique", false); //по умолчанию ложь
        } else {
            uriComponentsBuilder.queryParam("unique", unique);
        }

        return restClient.get()
                .uri(uriComponentsBuilder.build().toUri())
                .retrieve()
                .body(new ParameterizedTypeReference<List<StatsDto>>() {
                });
    }
}
