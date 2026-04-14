CREATE TABLE block_reactions (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    block_id UUID NOT NULL,
    member_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    CONSTRAINT pk_block_reactions PRIMARY KEY (id),
    CONSTRAINT fk_block_reactions_block FOREIGN KEY (block_id) REFERENCES blocks (id),
    CONSTRAINT fk_block_reactions_member FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT uk_block_reactions_block_member_type UNIQUE (block_id, member_id, type)
);
