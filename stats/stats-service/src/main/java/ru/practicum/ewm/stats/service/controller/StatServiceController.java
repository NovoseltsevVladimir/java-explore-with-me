package ru.practicum.service.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.service.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class StatServiceController {

    private StatService service;
    private final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    public StatServiceController(StatService service) {
        this.service = service;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitDto create(@Valid @RequestBody HitDto hitDto) {
        return service.createHit(hitDto);
    }

    @GetMapping("/stats")
    public List<StatsDto> getStats (@RequestParam @DateTimeFormat(pattern = DATE_FORMAT_PATTERN) LocalDateTime start,
                                    @RequestParam @DateTimeFormat(pattern = DATE_FORMAT_PATTERN) LocalDateTime end,
                                    @RequestParam (required = false) List<String> uris,
                                    @RequestParam (required = false, defaultValue = "false") Boolean unique) {
        return service.getStats(start,end,uris,unique);
    }

}
