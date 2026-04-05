package dev.jino.tripbasketnew.block.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;

public enum BlockStatus {
    BUCKET("bucket"),
    SCHEDULED("scheduled");

    private final String value;

    BlockStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static BlockStatus from(String value) {
        if (value == null) {
            return null;
        }

        for (BlockStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new BusinessException(ErrorCode.BLOCK_STATUS_INVALID);
    }
}
