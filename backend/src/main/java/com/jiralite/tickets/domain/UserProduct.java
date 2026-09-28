package com.jiralite.tickets.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_product")
public class UserProduct {

    @EmbeddedId
    private UserProductId id = new UserProductId();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductRole role;

    public UserProductId getId() {
        return id;
    }

    public void setId(UserProductId id) {
        this.id = id;
    }

    public ProductRole getRole() {
        return role;
    }

    public void setRole(ProductRole role) {
        this.role = role;
    }

    public static class UserProductId implements Serializable {
        @Column(name = "user_id", nullable = false)
        private UUID userId;

        @Column(name = "product_id", nullable = false)
        private UUID productId;

        public UserProductId() {}

        public UserProductId(UUID userId, UUID productId) {
            this.userId = userId;
            this.productId = productId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UserProductId that)) {
                return false;
            }
            return Objects.equals(userId, that.userId) && Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, productId);
        }
    }
}
