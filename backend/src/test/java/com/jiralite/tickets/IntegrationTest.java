package com.jiralite.tickets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiralite.tickets.domain.Product;
import com.jiralite.tickets.domain.ProductRole;
import com.jiralite.tickets.domain.User;
import com.jiralite.tickets.domain.UserProduct;
import com.jiralite.tickets.persistence.ProductRepository;
import com.jiralite.tickets.persistence.UserProductRepository;
import com.jiralite.tickets.persistence.UserRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTest {
    static final UUID SUPPORT = UUID.fromString("00000000-0000-0000-0000-000000000001");
    static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    static final UUID DANA = UUID.fromString("00000000-0000-0000-0000-0000000000d3");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    UserRepository users;

    @Autowired
    ProductRepository products;

    @Autowired
    UserProductRepository memberships;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void seed() {
        if (users.count() > 0) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        Product support = new Product();
        support.setId(SUPPORT);
        support.setName("Support");
        support.setCreatedAt(now);
        products.save(support);
        Product other = new Product();
        other.setId(OTHER);
        other.setName("Other");
        other.setCreatedAt(now);
        products.save(other);
        saveUser(ALICE, "alice", "alice@example.com", "Alice", now);
        saveUser(BOB, "bob", "bob@example.com", "Bob", now);
        saveUser(DANA, "dana", "dana@example.com", "Dana", now);
        link(ALICE, SUPPORT, ProductRole.PRODUCT_OWNER);
        link(BOB, SUPPORT, ProductRole.DEVELOPER);
        link(ALICE, OTHER, ProductRole.PRODUCT_MANAGER);
        link(DANA, OTHER, ProductRole.QA);
    }

    private void saveUser(UUID id, String username, String email, String name, OffsetDateTime now) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(encoder.encode("password"));
        user.setEmail(email);
        user.setDisplayName(name);
        user.setCreatedAt(now);
        users.save(user);
    }

    private void link(UUID userId, UUID productId, ProductRole role) {
        UserProduct membership = new UserProduct();
        membership.setId(new UserProduct.UserProductId(userId, productId));
        membership.setRole(role);
        memberships.save(membership);
    }

    protected String login(String username) throws Exception {
        MvcResult result =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"username\":\"" + username + "\",\"password\":\"password\"}"))
                        .andExpect(status().isOk())
                        .andReturn();
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    protected String createTicket(String token, String title) throws Exception {
        var result =
                mockMvc.perform(
                                post("/api/tickets")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"title\":\"" + title + "\"}"))
                        .andExpect(status().isCreated())
                        .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();
    }
}
