package dev.jino.tripbasketnew.block.dto;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateBlockRequestDtoDeserializationTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void missingMemo_deserializesToNullReference() throws Exception {
        UpdateBlockRequestDto request = objectMapper.readValue("{}", UpdateBlockRequestDto.class);

        assertThat(request.memo()).isNull();
    }

    @Test
    void explicitNullMemo_deserializesToNullNode() throws Exception {
        UpdateBlockRequestDto request = objectMapper.readValue("{\"memo\":null}", UpdateBlockRequestDto.class);

        assertThat(request.memo()).isSameAs(NullNode.getInstance());
    }
}
