package dev.jino.tripbasketnew.block.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.jino.tripbasketnew.block.entity.Block;
import dev.jino.tripbasketnew.block.entity.BlockStatus;

@Repository
public interface BlockRepository extends JpaRepository<Block, UUID> {

    @EntityGraph(attributePaths = {"room", "place", "addedBy"})
    List<Block> findAllByRoom_Id(UUID roomId);

    @EntityGraph(attributePaths = {"room", "place", "addedBy"})
    List<Block> findAllByRoom_IdAndStatus(UUID roomId, BlockStatus status);

    @EntityGraph(attributePaths = {"room", "place", "addedBy"})
    Optional<Block> findByIdAndRoom_Id(UUID blockId, UUID roomId);
}
