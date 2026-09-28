package com.jiralite.tickets.api;

import com.jiralite.tickets.api.dto.ApiResponse;
import com.jiralite.tickets.api.dto.UserResponse;
import com.jiralite.tickets.domain.User;
import com.jiralite.tickets.error.ApiException;
import com.jiralite.tickets.error.ErrorCodes;
import com.jiralite.tickets.error.MessageResolver;
import com.jiralite.tickets.service.MembershipService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final MembershipService memberships;
    private final MessageResolver messages;

    public UserController(MembershipService memberships, MessageResolver messages) {
        this.memberships = memberships;
        this.messages = messages;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> list(
            @RequestParam(required = false) UUID productId) {
        UUID actor = CurrentUser.id();
        List<User> users;
        if (productId != null) {
            if (!memberships.isMember(actor, productId)) {
                throw new ApiException(ErrorCodes.PRODUCT_ACCESS_DENIED, HttpStatus.FORBIDDEN);
            }
            users = memberships.usersForProduct(productId);
        } else {
            Map<UUID, User> unique = new LinkedHashMap<>();
            memberships.productsFor(actor).forEach(p -> memberships.usersForProduct(p.getId()).forEach(u -> unique.put(u.getId(), u)));
            users = List.copyOf(unique.values());
        }
        List<UserResponse> data =
                users.stream()
                        .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getDisplayName()))
                        .toList();
        return ResponseEntity.ok(ApiResponse.success(messages.message("SUCCESS"), "SUCCESS", data));
    }
}
