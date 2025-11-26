package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.practicum.ewm.stats.service.StatServiceApp;

@SpringBootApplication
public class ExploreWithMeServer {

    public static void main(String[] args) {
        SpringApplication.run(StatServiceApp.class, args);
    }
}
