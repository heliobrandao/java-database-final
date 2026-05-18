package com.project.code.Service;

import com.project.code.Model.Customer;
import com.project.code.Model.Inventory;
import com.project.code.Model.OrderDetails;
import com.project.code.Model.OrderItem;
import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Product;
import com.project.code.Model.PurchaseProductDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.OrderDetailsRepository;
import com.project.code.Repo.OrderItemRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Transactional
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {
        Customer customer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());
        if (customer == null) {
            customer = new Customer();
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer.setPhone(placeOrderRequest.getCustomerPhone());
            customer = customerRepository.save(customer);
        }

        Store store = storeRepository.findById(placeOrderRequest.getStoreId())
            .orElseThrow(() -> new RuntimeException("Store not found"));

        OrderDetails orderDetails = new OrderDetails(customer, store, placeOrderRequest.getTotalPrice(), LocalDateTime.now());
        OrderDetails savedOrderDetails = orderDetailsRepository.save(orderDetails);

        for (PurchaseProductDTO purchase : placeOrderRequest.getPurchaseProduct()) {
            Product product = productRepository.findByid(purchase.getId());
            if (product == null) {
                throw new RuntimeException("Product not found: " + purchase.getId());
            }

            Inventory inventory = inventoryRepository.findByProductIdandStoreId(product.getId(), store.getId());
            if (inventory == null) {
                throw new RuntimeException("Inventory not found for product: " + product.getId());
            }

            int currentStock = inventory.getStockLevel() == null ? 0 : inventory.getStockLevel();
            if (purchase.getQuantity() == null || purchase.getQuantity() <= 0) {
                throw new RuntimeException("Invalid quantity for product: " + product.getId());
            }
            if (currentStock < purchase.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getId());
            }

            inventory.setStockLevel(currentStock - purchase.getQuantity());
            inventoryRepository.save(inventory);

            OrderItem orderItem = new OrderItem(savedOrderDetails, product, purchase.getQuantity(), purchase.getPrice());
            orderItemRepository.save(orderItem);
        }
    }
}
