package com.jiralite.tickets.api;

import com.jiralite.tickets.api.dto.ApiResponse;
import com.jiralite.tickets.api.dto.ProductResponse;
import com.jiralite.tickets.error.MessageResolver;
import com.jiralite.tickets.service.MembershipService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final MembershipService memberships;
    private final MessageResolver messages;

    public ProductController(MembershipService memberships, MessageResolver messages) {
        this.memberships = memberships;
        this.messages = messages;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> list() {
        List<ProductResponse> data =
                memberships.productsFor(CurrentUser.id()).stream()
                        .map(p -> new ProductResponse(p.getId(), p.getName()))
                        .toList();
        return ResponseEntity.ok(ApiResponse.success(messages.message("SUCCESS"), "SUCCESS", data));
    }
}
