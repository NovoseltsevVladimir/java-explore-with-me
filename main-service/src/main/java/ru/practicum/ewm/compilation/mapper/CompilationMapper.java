package ru.practicum.ewm.compilation.mapper;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;

import java.util.Set;
import java.util.stream.Collectors;

public class CompilationMapper {

    public static CompilationDto mapToCompilationDto(Compilation compilation) {

        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setTitle(compilation.getTitle());
        dto.setPinned(compilation.getPinned());

        Set<EventShortDto> shortEvents = compilation.getEvents().stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toSet());
        dto.setEvents(shortEvents);

        return dto;
    }

    public static Compilation mapToCompilation(NewCompilationDto newCompilationDto, Set<Event> events) {

        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false);
        compilation.setEvents(events);

        return compilation;
    }
}
