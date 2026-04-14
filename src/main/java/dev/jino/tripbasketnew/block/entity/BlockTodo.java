package dev.jino.tripbasketnew.block.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.util.StringUtils;

import dev.jino.tripbasketnew.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "block_todos")
@SQLDelete(sql = "UPDATE block_todos SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class BlockTodo extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    public static BlockTodo create(Block block, String text) {
        BlockTodo todo = new BlockTodo();
        todo.block = block;
        todo.updateText(text);
        todo.completed = false;
        return todo;
    }

    public void updateText(String text) {
        this.text = StringUtils.hasText(text) ? text.strip() : "";
    }

    public void updateCompleted(boolean completed) {
        this.completed = completed;
    }
}
