package dev.jino.tripbasketnew.block.dto;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

class BlockTodoRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createTodo_requiresNonBlankText() {
        CreateBlockTodoRequestDto request = new CreateBlockTodoRequestDto("   ");

        Set<ConstraintViolation<CreateBlockTodoRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("text");
    }

    @Test
    void createTodo_rejectsNullText() {
        CreateBlockTodoRequestDto request = new CreateBlockTodoRequestDto(null);

        Set<ConstraintViolation<CreateBlockTodoRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("text");
    }

    @Test
    void createTodo_rejectsTextLongerThan1000Characters() {
        CreateBlockTodoRequestDto request = new CreateBlockTodoRequestDto("a".repeat(1001));

        Set<ConstraintViolation<CreateBlockTodoRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("text");
    }

    @Test
    void updateTodo_allowsMissingText() {
        UpdateBlockTodoRequestDto request = new UpdateBlockTodoRequestDto(null, true);

        Set<ConstraintViolation<UpdateBlockTodoRequestDto>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void updateTodo_rejectsBlankText() {
        UpdateBlockTodoRequestDto request = new UpdateBlockTodoRequestDto("   ", null);

        Set<ConstraintViolation<UpdateBlockTodoRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("text");
    }

    @Test
    void updateTodo_rejectsTextLongerThan1000Characters() {
        UpdateBlockTodoRequestDto request = new UpdateBlockTodoRequestDto("a".repeat(1001), null);

        Set<ConstraintViolation<UpdateBlockTodoRequestDto>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("text");
    }
}
