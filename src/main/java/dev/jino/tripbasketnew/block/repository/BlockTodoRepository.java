package dev.jino.tripbasketnew.block.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.jino.tripbasketnew.block.entity.BlockTodo;

@Repository
public interface BlockTodoRepository extends JpaRepository<BlockTodo, UUID> {

    List<BlockTodo> findAllByBlock_Id(UUID blockId);

    List<BlockTodo> findAllByBlock_IdOrderByCreatedAtAsc(UUID blockId);

    Optional<BlockTodo> findByIdAndBlock_Id(UUID todoId, UUID blockId);
}
