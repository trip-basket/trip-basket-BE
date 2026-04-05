package dev.jino.tripbasketnew.block.dto.validation;

import java.time.OffsetDateTime;

import dev.jino.tripbasketnew.block.dto.CreateBlockRequestDto;
import dev.jino.tripbasketnew.block.entity.BlockStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CreateBlockRequestValidator
        implements ConstraintValidator<ValidCreateBlockRequest, CreateBlockRequestDto> {

    @Override
    public boolean isValid(CreateBlockRequestDto value, ConstraintValidatorContext context) {
        if (value == null || value.status() == null) {
            return true;
        }

        if (value.status() == BlockStatus.SCHEDULED) {
            return validateScheduled(value.startTime(), value.endTime(), context);
        }

        return validateBucket(value.startTime(), value.endTime(), context);
    }

    private boolean validateScheduled(
            OffsetDateTime startTime, OffsetDateTime endTime, ConstraintValidatorContext context) {
        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (startTime == null) {
            addViolation(context, "startTime", "status가 scheduled일 때 startTime은 필수입니다.");
            valid = false;
        }

        if (endTime == null) {
            addViolation(context, "endTime", "status가 scheduled일 때 endTime은 필수입니다.");
            valid = false;
        }

        if (!valid) {
            return false;
        }

        if (!endTime.isAfter(startTime)) {
            addViolation(context, "endTime", "endTime은 startTime보다 뒤여야 합니다.");
            return false;
        }

        return true;
    }

    private boolean validateBucket(
            OffsetDateTime startTime, OffsetDateTime endTime, ConstraintValidatorContext context) {
        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (startTime != null) {
            addViolation(context, "startTime", "status가 bucket일 때 startTime은 null이어야 합니다.");
            valid = false;
        }

        if (endTime != null) {
            addViolation(context, "endTime", "status가 bucket일 때 endTime은 null이어야 합니다.");
            valid = false;
        }

        return valid;
    }

    private void addViolation(ConstraintValidatorContext context, String propertyName, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyName)
                .addConstraintViolation();
    }
}
