package dev.jino.tripbasketnew.place.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import dev.jino.tripbasketnew.common.entity.SoftDeletableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "places")
@SQLDelete(sql = "UPDATE places SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class Place extends SoftDeletableEntity {

    @Column(name = "google_place_id", nullable = false, unique = true)
    private String googlePlaceId;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Column(name = "category")
    private String category;

    @Column(name = "formatted_address")
    private String formattedAddress;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "price_level")
    private Integer priceLevel;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "timezone_id")
    private String timezoneId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "place_opening_hours", joinColumns = @JoinColumn(name = "place_id"))
    @OrderColumn(name = "sort_order")
    @Builder.Default
    private List<PlaceOpeningHour> openingHours = new ArrayList<>();

    public void updateDetails(
            String placeName,
            Double lat,
            Double lng,
            String category,
            String formattedAddress,
            Double rating,
            Integer reviewCount,
            Integer priceLevel,
            String photoUrl,
            String timezoneId,
            List<PlaceOpeningHour> openingHours) {
        this.placeName = placeName;
        this.lat = lat;
        this.lng = lng;
        this.category = category;
        this.formattedAddress = formattedAddress;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.priceLevel = priceLevel;
        this.photoUrl = photoUrl;
        this.timezoneId = timezoneId;
        this.openingHours = openingHours == null ? new ArrayList<>() : new ArrayList<>(openingHours);
    }
}
