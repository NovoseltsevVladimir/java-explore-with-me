package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class StatsDto {

    @NotBlank
    private String app;
    @NotBlank
    private String uri;
    private Integer hit = 0;

}
