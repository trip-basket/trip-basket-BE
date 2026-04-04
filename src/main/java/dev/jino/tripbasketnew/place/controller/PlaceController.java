package dev.jino.tripbasketnew.place.controller;

import org.springframework.web.bind.annotation.RestController;

import dev.jino.tripbasketnew.place.controller.api.PlaceApi;
import dev.jino.tripbasketnew.place.dto.PlaceDetailResponseDto;
import dev.jino.tripbasketnew.place.service.PlaceService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PlaceController implements PlaceApi {

    private final PlaceService placeService;

    @Override
    public PlaceDetailResponseDto getPlaceDetail(String placeId) {
        return placeService.getPlaceDetail(placeId);
    }
}
