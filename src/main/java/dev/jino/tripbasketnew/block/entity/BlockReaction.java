package dev.jino.tripbasketnew.block.entity;

import dev.jino.tripbasketnew.common.entity.BaseEntity;
import dev.jino.tripbasketnew.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "block_reactions",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_block_reactions_block_member_type",
                        columnNames = {"block_id", "member_id", "type"}))
public class BlockReaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BlockReactionType type;

    public static BlockReaction create(Block block, Member member, BlockReactionType type) {
        BlockReaction reaction = new BlockReaction();
        reaction.block = block;
        reaction.member = member;
        reaction.type = type;
        return reaction;
    }
}
