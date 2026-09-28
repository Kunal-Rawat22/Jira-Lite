package com.jiralite.tickets.persistence;

import com.jiralite.tickets.domain.UserProduct;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProductRepository extends JpaRepository<UserProduct, UserProduct.UserProductId> {
    List<UserProduct> findByIdUserId(UUID userId);

    List<UserProduct> findByIdProductId(UUID productId);

    boolean existsByIdUserIdAndIdProductId(UUID userId, UUID productId);
}
