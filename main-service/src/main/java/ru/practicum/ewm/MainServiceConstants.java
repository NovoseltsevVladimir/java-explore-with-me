package ru.practicum.ewm;

import java.time.LocalDateTime;

public class MainServiceConstants {
    public static final String DATA_DTO_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final LocalDateTime START_DEFAULT_DATE
            = LocalDateTime.of(2000, 01, 01, 01, 01);
    public static final LocalDateTime END_DEFAULT_DATE
            = LocalDateTime.of(3999, 12, 31, 23, 59);
}
