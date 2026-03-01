package dev.jino.tripbasketnew.place.controller;

import dev.jino.tripbasketnew.place.dto.PlaceDetailResponseDto;
import dev.jino.tripbasketnew.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
@Tag(name = "Place", description = "Google Place detail API")
public class PlaceController {

    private final PlaceService placeService;

    @Operation(
        summary = "장소 상세 조회",
        description = "placeId로 Google Places 상세 정보를 조회합니다. 로그인된 사용자만 호출 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PlaceDetailResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 placeId", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "404", description = "장소를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "502", description = "Google Places 연동 오류", content = @Content)
    })
    @GetMapping("/{placeId}")
    public PlaceDetailResponseDto getPlaceDetail(
        @Parameter(
            description = "Google Place ID",
            example = "ChIJdd4hrwug2EcRmSrV3Vo6llI"
        )
        @PathVariable String placeId
    ) {
        return placeService.getPlaceDetail(placeId);
    }
}
