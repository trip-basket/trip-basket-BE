CREATE TABLE block_todos (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL,
    deleted_at TIMESTAMP,
    block_id UUID NOT NULL,
    text VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL,
    CONSTRAINT pk_block_todos PRIMARY KEY (id),
    CONSTRAINT fk_block_todos_block FOREIGN KEY (block_id) REFERENCES blocks (id)
);
