package dev.jino.tripbasketnew.room.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import dev.jino.tripbasketnew.common.entity.SoftDeletableEntity;
import dev.jino.tripbasketnew.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "room_members")
@SQLDelete(sql = "UPDATE room_members SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class RoomMember extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    private RoomRole role;

    public static RoomMember owner(Room room, Member member) {
        return RoomMember.builder()
                .room(room)
                .member(member)
                .role(RoomRole.OWNER)
                .build();
    }

    public static RoomMember member(Room room, Member member) {
        return RoomMember.builder()
                .room(room)
                .member(member)
                .role(RoomRole.MEMBER)
                .build();
    }
}
