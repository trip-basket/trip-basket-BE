package dev.jino.tripbasketnew.block.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.jino.tripbasketnew.block.entity.Block;

@Repository
public interface BlockRepository extends JpaRepository<Block, UUID> {}
