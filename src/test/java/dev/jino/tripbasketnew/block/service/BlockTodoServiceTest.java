package dev.jino.tripbasketnew.block.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jino.tripbasketnew.block.dto.CreateBlockTodoRequestDto;
import dev.jino.tripbasketnew.block.dto.UpdateBlockTodoRequestDto;
import dev.jino.tripbasketnew.block.entity.Block;
import dev.jino.tripbasketnew.block.entity.BlockStatus;
import dev.jino.tripbasketnew.block.entity.BlockTodo;
import dev.jino.tripbasketnew.block.repository.BlockRepository;
import dev.jino.tripbasketnew.block.repository.BlockTodoRepository;
import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.place.entity.Place;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockTodoServiceTest {

    @Mock
    private BlockTodoRepository blockTodoRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private RoomAccessPolicy roomAccessPolicy;

    private BlockTodoService blockTodoService;

    @BeforeEach
    void setUp() {
        blockTodoService = new BlockTodoService(blockTodoRepository, blockRepository, roomAccessPolicy);
    }

    @Test
    void getTodoResponses_returnsTodosInRepositoryOrder() {
        Block block = block();
        BlockTodo first = BlockTodo.create(block, "오디오 가이드 대여");
        BlockTodo second = BlockTodo.create(block, "기념품샵 들르기");

        when(blockTodoRepository.findAllByBlock_IdOrderByCreatedAtAsc(block.getId()))
                .thenReturn(List.of(first, second));

        var response = blockTodoService.getTodoResponses(block.getId());

        assertThat(response).extracting("text").containsExactly("오디오 가이드 대여", "기념품샵 들르기");
    }

    @Test
    void createTodo_savesIncompleteTodo() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockTodoRepository.save(any(BlockTodo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = blockTodoService.createTodo(
                room.getId(), block.getId(), new CreateBlockTodoRequestDto("오디오 가이드 대여"), member.getId());

        assertThat(response.text()).isEqualTo("오디오 가이드 대여");
        assertThat(response.completed()).isFalse();
    }

    @Test
    void updateTodo_updatesTextAndCompleted() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);
        BlockTodo todo = BlockTodo.create(block, "기존 투두");
        UUID todoId = UUID.randomUUID();

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockTodoRepository.findByIdAndBlock_Id(todoId, block.getId())).thenReturn(Optional.of(todo));

        var response = blockTodoService.updateTodo(
                room.getId(), block.getId(), todoId, new UpdateBlockTodoRequestDto("변경된 투두", true), member.getId());

        assertThat(response.text()).isEqualTo("변경된 투두");
        assertThat(response.completed()).isTrue();
    }

    @Test
    void updateTodo_throwsWhenTodoDoesNotExist() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);
        UUID missingTodoId = UUID.randomUUID();

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockTodoRepository.findByIdAndBlock_Id(missingTodoId, block.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> blockTodoService.updateTodo(
                        room.getId(),
                        block.getId(),
                        missingTodoId,
                        new UpdateBlockTodoRequestDto("변경", null),
                        member.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BLOCK_TODO_NOT_FOUND);
    }

    @Test
    void deleteTodo_softDeletesTodo() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);
        BlockTodo todo = BlockTodo.create(block, "삭제할 투두");
        UUID todoId = UUID.randomUUID();

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockTodoRepository.findByIdAndBlock_Id(todoId, block.getId())).thenReturn(Optional.of(todo));

        blockTodoService.deleteTodo(room.getId(), block.getId(), todoId, member.getId());

        verify(blockTodoRepository).delete(todo);
    }

    @Test
    void softDeleteByBlockId_deletesAllTodosForBlock() {
        Block block = block();
        BlockTodo first = BlockTodo.create(block, "첫번째");
        BlockTodo second = BlockTodo.create(block, "두번째");

        when(blockTodoRepository.findAllByBlock_Id(block.getId())).thenReturn(List.of(first, second));

        blockTodoService.softDeleteByBlockId(block.getId());

        verify(blockTodoRepository).deleteAll(List.of(first, second));
    }

    @Test
    void softDeleteByBlockId_skipsDeleteWhenTodoIsEmpty() {
        Block block = block();

        when(blockTodoRepository.findAllByBlock_Id(block.getId())).thenReturn(List.of());

        blockTodoService.softDeleteByBlockId(block.getId());

        verify(blockTodoRepository, never()).deleteAll(any());
    }

    private Room room() {
        return Room.builder()
                .id(UUID.randomUUID())
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 4, 5))
                .tripEndDate(LocalDate.of(2026, 4, 10))
                .build();
    }

    private Member member() {
        return Member.builder()
                .id(UUID.randomUUID())
                .email("jino@example.com")
                .nickname("jino")
                .build();
    }

    private Block block() {
        return block(room(), member());
    }

    private Block block(Room room, Member member) {
        return Block.create(
                room,
                Place.builder()
                        .googlePlaceId("google-place-id")
                        .placeName("대영박물관")
                        .timezoneId("Europe/London")
                        .build(),
                member,
                BlockStatus.BUCKET,
                "후보",
                null,
                null,
                "Europe/London",
                java.time.OffsetDateTime.now());
    }
}
