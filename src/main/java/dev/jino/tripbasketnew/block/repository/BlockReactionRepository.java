package dev.jino.tripbasketnew.block.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.jino.tripbasketnew.block.entity.BlockReaction;
import dev.jino.tripbasketnew.block.entity.BlockReactionType;

@Repository
public interface BlockReactionRepository extends JpaRepository<BlockReaction, UUID> {

    List<BlockReaction> findAllByBlock_IdOrderByCreatedAtAsc(UUID blockId);

    boolean existsByBlock_IdAndMember_IdAndType(UUID blockId, UUID memberId, BlockReactionType type);

    Optional<BlockReaction> findByIdAndBlock_Id(UUID reactionId, UUID blockId);

    void deleteAllByBlock_Id(UUID blockId);
}
