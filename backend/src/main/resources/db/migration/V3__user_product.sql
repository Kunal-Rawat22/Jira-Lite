CREATE TABLE user_product (
    user_id UUID NOT NULL REFERENCES users (id),
    product_id UUID NOT NULL REFERENCES products (id),
    role VARCHAR(64) NOT NULL,
    PRIMARY KEY (user_id, product_id)
);
