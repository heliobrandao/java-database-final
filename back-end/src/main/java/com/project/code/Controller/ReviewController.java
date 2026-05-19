package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // GET /reviews/{storeId}/{productId} 
    @GetMapping("/{storeId}/{productId}")
    public Map<String, Object> getReviews(@PathVariable long storeId, @PathVariable long productId) {
        List<Review> reviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);
        List<Map<String, Object>> reviewsWithNames = new ArrayList<>();

        for (Review review : reviews) {
            Map<String, Object> item = new HashMap<>();
            item.put("comment", review.getComment());
            item.put("rating", review.getRating());

            // dynamiczne pobranie nazwy klienta
            Customer customer = customerRepository.findByid(review.getCustomerId());
            String customerName = (customer != null) ? customer.getName() : "Unknown";
            item.put("customerName", customerName);

            reviewsWithNames.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewsWithNames);
        return response;
    }

    // endpoint
    @GetMapping
    public Map<String, Object> getAllReviews() {
        List<Review> all = reviewRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", all);
        return response;
    }
}
