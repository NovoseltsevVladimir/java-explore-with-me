package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"ru.practicum.ewm", "ru.practicum.stats.client"})
public class ExploreWithMeServer {

    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeServer.class, args);
    }
}
