package com.project.code.Controller;

import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.OrderItemRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ServiceClass serviceClass;

    @PostMapping
    public Map<String, String> addProduct(@RequestBody Product product) {
        Map<String, String> map = new HashMap<>();
        if (!serviceClass.validateProduct(product)) {
            map.put("message", "Product already present");
            return map;
        }
        try {
            productRepository.save(product);
            map.put("message", "Product added successfully");
        } catch (DataIntegrityViolationException e) {
            map.put("message", "SKU must be unique");
        }
        return map;
    }

    // GET /product/{id} (3 punkty)
    @GetMapping("/product/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        Map<String, Object> map = new HashMap<>();
        Product product = productRepository.findByid(id);
        if (product == null) {
            map.put("error", "Product not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        }
        map.put("products", product);
        return ResponseEntity.ok(map);
    }

    // DELETE /product/{id} (3 punkty)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        Map<String, String> map = new HashMap<>();
        if (!serviceClass.ValidateProductId(id)) {
            map.put("message", "Product not present");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        }
        // Kaskada: najpierw usuń inventory, potem produkt
        inventoryRepository.deleteByProductId(id);
        orderItemRepository.deleteByProductId(id);
        productRepository.deleteById(id);
        map.put("message", "Deleted product and its inventory");
        return ResponseEntity.ok(map);
    }

    // pozostałe metody (update, filter, list, search) – zgodne z rozwiązaniem
    @PutMapping
    public Map<String, String> updateProduct(@RequestBody Product product) {
        Map<String, String> map = new HashMap<>();
        productRepository.save(product);
        map.put("message", "Product updated");
        return map;
    }

    @GetMapping
    public Map<String, Object> listProducts() {
        Map<String, Object> map = new HashMap<>();
        map.put("products", productRepository.findAll());
        return map;
    }

    @GetMapping("/searchProduct/{name}")
    public Map<String, Object> searchProduct(@PathVariable String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("products", productRepository.findProductBySubName(name));
        return map;
    }
}
