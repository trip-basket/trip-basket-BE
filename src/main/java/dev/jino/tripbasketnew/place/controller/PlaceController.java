package dev.jino.tripbasketnew.place.controller;

import dev.jino.tripbasketnew.place.dto.PlaceDetailResponseDto;
import dev.jino.tripbasketnew.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/{placeId}")
    public PlaceDetailResponseDto getPlaceDetail(@PathVariable String placeId) {
        return placeService.getPlaceDetail(placeId);
    }
}
