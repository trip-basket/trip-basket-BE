package dev.jino.tripbasketnew.place.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.jino.tripbasketnew.place.entity.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, UUID> {

    Optional<Place> findByGooglePlaceId(String googlePlaceId);
}
