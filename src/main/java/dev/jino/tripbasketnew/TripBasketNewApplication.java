package dev.jino.tripbasketnew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TripBasketNewApplication {

    public static void main(String[] args) {
        SpringApplication.run(TripBasketNewApplication.class, args);
    }
}
