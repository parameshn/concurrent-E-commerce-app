package com.example.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.ok(createdProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Optional<Product> product = productService.getProduct(id);
        return product.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<Product>>> getAllProducts() {
        return productService.getAllProductsAsync()
                .thenApply(ResponseEntity::ok);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<String> updateStock(@PathVariable Long id, @RequestParam Integer quantity)
    {
        productService.updateStock(id, quantity);
        return ResponseEntity.ok("Stock updated successfully");
    }

    @PostMapping("/batch-update-prices")
    public ResponseEntity<String> batchUpdatePrices(@RequestBody List<Long> productIds,
            @RequestParam Double multiplier) {
        productService.batchUpdatePrices(productIds, multiplier);
        return ResponseEntity.ok("Batch update started asynchronously");
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam(defaultValue = "10") Integer threshold) {
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/stats/operations")
    public ResponseEntity<Long> getOperationCount() {
        return ResponseEntity.ok(productService.getOperationCount());
    }
             
}
