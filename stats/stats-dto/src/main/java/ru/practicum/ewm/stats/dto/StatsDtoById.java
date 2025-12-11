package ru.practicum.ewm.stats.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@RequiredArgsConstructor
public class StatsDtoById {
    private HashMap<Long,Long> idAndViews = new HashMap<>();
}
