package dev.jino.tripbasketnew.room.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import dev.jino.tripbasketnew.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "rooms")
@SQLDelete(sql = "UPDATE rooms SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class Room extends SoftDeletableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "trip_start_date", nullable = false)
    private LocalDate tripStartDate;

    @Column(name = "trip_end_date", nullable = false)
    private LocalDate tripEndDate;

    @Column(name = "invite_code")
    private String inviteCode;

    @Column(name = "invite_code_issued_at")
    private LocalDateTime inviteCodeIssuedAt;

    public void update(String name, LocalDate tripStartDate, LocalDate tripEndDate) {
        this.name = name;
        this.tripStartDate = tripStartDate;
        this.tripEndDate = tripEndDate;
    }

    public void issueInviteCode(String inviteCode, LocalDateTime issuedAt) {
        this.inviteCode = inviteCode;
        this.inviteCodeIssuedAt = issuedAt;
    }
}
