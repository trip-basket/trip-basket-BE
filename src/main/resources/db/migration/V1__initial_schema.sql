CREATE TABLE members (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL,
    deleted_at TIMESTAMP,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    CONSTRAINT pk_members PRIMARY KEY (id),
    CONSTRAINT uk_members_email UNIQUE (email)
);

CREATE TABLE places (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL,
    deleted_at TIMESTAMP,
    google_place_id VARCHAR(255) NOT NULL,
    place_name VARCHAR(255) NOT NULL,
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    category VARCHAR(255),
    formatted_address VARCHAR(255),
    rating DOUBLE PRECISION,
    review_count INTEGER,
    price_level INTEGER,
    photo_url VARCHAR(255),
    timezone_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_places PRIMARY KEY (id),
    CONSTRAINT uk_places_google_place_id UNIQUE (google_place_id)
);

CREATE TABLE rooms (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL,
    deleted_at TIMESTAMP,
    name VARCHAR(255) NOT NULL,
    trip_start_date DATE NOT NULL,
    trip_end_date DATE NOT NULL,
    invite_code VARCHAR(255),
    invite_code_issued_at TIMESTAMP,
    CONSTRAINT pk_rooms PRIMARY KEY (id)
);

CREATE TABLE room_members (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL,
    deleted_at TIMESTAMP,
    room_id UUID NOT NULL,
    member_id UUID NOT NULL,
    role VARCHAR(255),
    CONSTRAINT pk_room_members PRIMARY KEY (id),
    CONSTRAINT fk_room_members_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT fk_room_members_member FOREIGN KEY (member_id) REFERENCES members (id)
);

CREATE TABLE blocks (
    id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL,
    deleted_at TIMESTAMP,
    room_id UUID NOT NULL,
    place_id UUID NOT NULL,
    added_by_member_id UUID NOT NULL,
    status VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    timezone_id VARCHAR(255) NOT NULL,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_blocks PRIMARY KEY (id),
    CONSTRAINT fk_blocks_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT fk_blocks_place FOREIGN KEY (place_id) REFERENCES places (id),
    CONSTRAINT fk_blocks_added_by_member FOREIGN KEY (added_by_member_id) REFERENCES members (id)
);

CREATE TABLE place_opening_hours (
    place_id UUID NOT NULL,
    sort_order INTEGER NOT NULL,
    day INTEGER NOT NULL,
    open_at TIME,
    close_at TIME,
    CONSTRAINT pk_place_opening_hours PRIMARY KEY (place_id, sort_order),
    CONSTRAINT fk_place_opening_hours_place FOREIGN KEY (place_id) REFERENCES places (id)
);
