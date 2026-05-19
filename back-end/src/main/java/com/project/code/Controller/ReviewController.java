package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class ReviewController {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/reviews")
    public Map<String, Object> getAllReviews() {
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewRepository.findAll());
        return response;
    }

    @GetMapping("/{storeId}/{productId}")
    public Map<String, Object> getReviews(@PathVariable Long storeId, @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        List<Review> reviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);
        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Review review : reviews) {
            Map<String, Object> row = new HashMap<>();
            row.put("comment", review.getComment());
            row.put("rating", review.getRating());

            Customer customer = customerRepository.findById(review.getCustomerId() == null ? -1L : review.getCustomerId());
            row.put("customerName", customer != null ? customer.getName() : "Unknown");
            filtered.add(row);
        }

        response.put("reviews", filtered);
        return response;
    }

    @GetMapping("/reviews/{storeId}/{productId}")
    public Map<String, Object> getReviewsWithPrefix(@PathVariable Long storeId, @PathVariable Long productId) {
        return getReviews(storeId, productId);
    }
}
