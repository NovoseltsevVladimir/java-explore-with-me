package ru.practicum.ewm.stats.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.dto.StatsDto;
import ru.practicum.ewm.stats.service.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatServiceRepository extends JpaRepository<Hit, Integer> {

    @Query("""
            SELECT new ru.practicum.ewm.stats.dto.StatsDto(hit.app, hit.uri, COUNT(DISTINCT hit.ip))
            FROM Hit hit
            WHERE hit.timestamp BETWEEN :start AND :end
            GROUP BY hit.app, hit.uri
            ORDER BY COUNT(DISTINCT hit.ip) DESC
            """)
    List<StatsDto> getUniqueStatsByDates(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("""
            SELECT new ru.practicum.ewm.stats.dto.StatsDto(hit.app, hit.uri, COUNT(hit))
            FROM Hit hit
            WHERE hit.timestamp BETWEEN :start AND :end
            GROUP BY hit.app, hit.uri
            ORDER BY COUNT(hit) DESC
            """)
    List<StatsDto> getNotUniqueStatsByDates(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);


    @Query("""
            SELECT new ru.practicum.ewm.stats.dto.StatsDto(hit.app, hit.uri, COUNT(hit))
            FROM Hit hit
            WHERE hit.timestamp BETWEEN :start AND :end
            AND hit.uri IN :uris
            GROUP BY hit.app, hit.uri
            ORDER BY COUNT(DISTINCT hit.ip) DESC
            """)
    List<StatsDto> getUniqueStatsByDatesAndUris(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end,
                                                @Param("uris") List<String> uris);

    @Query("""
            SELECT new ru.practicum.ewm.stats.dto.StatsDto(hit.app, hit.uri, COUNT(hit))
            FROM Hit hit
            WHERE hit.timestamp BETWEEN :start AND :end
            AND hit.uri IN :uris
            GROUP BY hit.app, hit.uri
            ORDER BY COUNT(hit) DESC
            """)
    List<StatsDto> getNotUniqueStatsByDatesAndUris(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("uris") List<String> uris);

}
