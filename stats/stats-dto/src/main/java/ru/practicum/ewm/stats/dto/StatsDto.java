package ru.practicum.ewm.stats.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StatsDto {

    @NotBlank
    private String app;
    @NotBlank
    private String uri;
    private Long hit = 0L;

}
