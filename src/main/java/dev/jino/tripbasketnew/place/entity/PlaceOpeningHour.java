package dev.jino.tripbasketnew.place.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceOpeningHour {

    @Column(name = "day", nullable = false)
    private Integer day;

    @Column(name = "open_at")
    private LocalTime openAt;

    @Column(name = "close_at")
    private LocalTime closeAt;

    private PlaceOpeningHour(Integer day, LocalTime openAt, LocalTime closeAt) {
        this.day = day;
        this.openAt = openAt;
        this.closeAt = closeAt;
    }

    public static PlaceOpeningHour of(Integer day, LocalTime openAt, LocalTime closeAt) {
        return new PlaceOpeningHour(day, openAt, closeAt);
    }
}
