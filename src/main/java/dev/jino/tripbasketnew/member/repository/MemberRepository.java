package dev.jino.tripbasketnew.member.repository;

import dev.jino.tripbasketnew.member.entity.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

    Optional<Member> findByEmail(String email);
}
