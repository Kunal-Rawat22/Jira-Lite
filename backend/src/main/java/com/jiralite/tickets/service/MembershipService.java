package com.jiralite.tickets.service;

import com.jiralite.tickets.domain.Product;
import com.jiralite.tickets.domain.User;
import com.jiralite.tickets.domain.UserProduct;
import com.jiralite.tickets.persistence.ProductRepository;
import com.jiralite.tickets.persistence.UserProductRepository;
import com.jiralite.tickets.persistence.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MembershipService {
    private final UserProductRepository memberships;
    private final ProductRepository products;
    private final UserRepository users;

    public MembershipService(
            UserProductRepository memberships, ProductRepository products, UserRepository users) {
        this.memberships = memberships;
        this.products = products;
        this.users = users;
    }

    public List<UserProduct> membershipsOf(UUID userId) {
        return memberships.findByIdUserId(userId);
    }

    public boolean isMember(UUID userId, UUID productId) {
        return memberships.existsByIdUserIdAndIdProductId(userId, productId);
    }

    public List<Product> productsFor(UUID userId) {
        return membershipsOf(userId).stream()
                .map(m -> products.findById(m.getId().getProductId()).orElseThrow())
                .toList();
    }

    public List<User> usersForProduct(UUID productId) {
        return memberships.findByIdProductId(productId).stream()
                .map(m -> users.findById(m.getId().getUserId()).orElseThrow())
                .toList();
    }
}
