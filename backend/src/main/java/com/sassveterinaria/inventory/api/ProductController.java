package com.sassveterinaria.inventory.api;

import com.sassveterinaria.inventory.dto.ProductCreateRequest;
import com.sassveterinaria.inventory.dto.ProductPatchRequest;
import com.sassveterinaria.inventory.dto.ProductResponse;
import com.sassveterinaria.inventory.dto.ProductStockResponse;
import com.sassveterinaria.inventory.service.InventoryService;
import com.sassveterinaria.security.AuthPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final InventoryService inventoryService;

    public ProductController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ProductResponse> create(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestBody ProductCreateRequest request
    ) {
        return ResponseEntity.ok(inventoryService.createProduct(principal, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductResponse>> list(
        @AuthenticationPrincipal AuthPrincipal principal,
        @RequestParam(value = "q", required = false) String q,
        @RequestParam(value = "active", required = false) Boolean active,
        @RequestParam(value = "lowStock", required = false) Boolean lowStock
    ) {
        return ResponseEntity.ok(inventoryService.listProducts(principal, q, active, lowStock));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ProductResponse> get(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID productId
    ) {
        return ResponseEntity.ok(inventoryService.getProduct(principal, productId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ProductResponse> patch(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID productId,
        @RequestBody ProductPatchRequest request
    ) {
        return ResponseEntity.ok(inventoryService.patchProduct(principal, productId, request));
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public ResponseEntity<ProductStockResponse> getStock(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID productId
    ) {
        return ResponseEntity.ok(inventoryService.getProductStock(principal, productId));
    }
}
