package com.project.code.Controller;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ServiceClass serviceClass;

    @PutMapping
    public Map<String, String> updateInventory(@RequestBody CombinedRequest request) {
        // ... (jak w rozwiązaniu)
        Map<String, String> map = new HashMap<>();
        Product product = request.getProduct();
        Inventory inventory = request.getInventory();
        if (!serviceClass.ValidateProductId(product.getId())) {
            map.put("message", "Product not found");
            return map;
        }
        productRepository.save(product);
        if (inventory != null) {
            Inventory existing = serviceClass.getInventoryId(inventory);
            if (existing != null) {
                inventory.setId(existing.getId());
                inventoryRepository.save(inventory);
            }
        }
        map.put("message", "Success");
        return map;
    }

    @PostMapping
    public Map<String, String> saveInventory(@RequestBody Inventory inventory) {
        Map<String, String> map = new HashMap<>();
        if (serviceClass.validateInventory(inventory)) {
            inventoryRepository.save(inventory);
            map.put("message", "Inventory saved");
        } else {
            map.put("message", "Inventory already exists");
        }
        return map;
    }

    @GetMapping("/{storeid}")
    public Map<String, Object> getAllProducts(@PathVariable Long storeid) {
        Map<String, Object> map = new HashMap<>();
        map.put("products", productRepository.findProductsByStoreId(storeid));
        return map;
    }

    // GET /filter/{category}/{name}/{storeId} (4 punkty)
    @GetMapping("filter/{category}/{name}/{storeid}")
    public ResponseEntity<Map<String, Object>> getProductName(
            @PathVariable String category,
            @PathVariable String name,
            @PathVariable long storeid) {

        Map<String, Object> map = new HashMap<>();
        // Walidacja: jeśli storeid <=0, zwróć błąd
        if (storeid <= 0) {
            map.put("error", "Invalid store ID");
            return ResponseEntity.badRequest().body(map);
        }

        List<Product> products;
        if (category.equals("null") && !name.equals("null")) {
            products = productRepository.findByNameLike(storeid, name);
        } else if (name.equals("null") && !category.equals("null")) {
            products = productRepository.findByCategoryAndStoreId(storeid, category);
        } else if (!category.equals("null") && !name.equals("null")) {
            products = productRepository.findByNameAndCategory(storeid, name, category);
        } else {
            products = List.of(); // oba null – brak wyników
        }
        map.put("product", products);
        return ResponseEntity.ok(map);
    }

    // GET /validate/{quantity}/{storeId}/{productId} (4 punkty)
    @GetMapping("validate/{quantity}/{storeId}/{productId}")
    public ResponseEntity<Boolean> validateQuantity(
            @PathVariable int quantity,
            @PathVariable long storeId,
            @PathVariable long productId) {

        if (quantity <= 0 || storeId <= 0 || productId <= 0) {
            return ResponseEntity.badRequest().body(false);
        }
        Inventory inv = inventoryRepository.findByProductIdandStoreId(productId, storeId);
        boolean available = inv != null && inv.getStockLevel() >= quantity;
        return ResponseEntity.ok(available);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> removeProduct(@PathVariable Long id) {
        Map<String, String> map = new HashMap<>();
        inventoryRepository.deleteByProductId(id);
        map.put("message", "Deleted");
        return map;
    }
}
