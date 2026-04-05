package dev.jino.tripbasketnew.block.dto;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.jino.tripbasketnew.block.entity.BlockStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

class CreateBlockRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void scheduledStatus_requiresStartTimeAndEndTime() {
        CreateBlockRequestDto request =
                new CreateBlockRequestDto(BlockStatus.SCHEDULED, "google-place-id", "대영박물관 관람", null, null);

        Set<ConstraintViolation<CreateBlockRequestDto>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("startTime", "endTime");
    }

    @Test
    void bucketStatus_disallowsStartTimeAndEndTime() {
        CreateBlockRequestDto request = new CreateBlockRequestDto(
                BlockStatus.BUCKET,
                "google-place-id",
                "대영박물관 관람",
                LocalDateTime.of(2026, 4, 5, 10, 0),
                LocalDateTime.of(2026, 4, 5, 11, 0));

        Set<ConstraintViolation<CreateBlockRequestDto>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("startTime", "endTime");
    }

    @Test
    void scheduledStatus_requiresEndTimeAfterStartTime() {
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 5, 10, 0);
        CreateBlockRequestDto request =
                new CreateBlockRequestDto(BlockStatus.SCHEDULED, "google-place-id", "대영박물관 관람", startTime, startTime);

        Set<ConstraintViolation<CreateBlockRequestDto>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("endTime");
    }

    @Test
    void validBucketRequest_passesValidation() {
        CreateBlockRequestDto request =
                new CreateBlockRequestDto(BlockStatus.BUCKET, "google-place-id", "대영박물관 관람", null, null);

        Set<ConstraintViolation<CreateBlockRequestDto>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
