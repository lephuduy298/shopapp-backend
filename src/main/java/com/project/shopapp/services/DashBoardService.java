package com.project.shopapp.services;

import com.project.shopapp.dto.res.ResDashBoardStats;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.iservice.IDashBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashBoardService implements IDashBoardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public ResDashBoardStats getDashboardStats() {
        long totalOrders = orderRepository.count();
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalUsers = userRepository.count();

        return new ResDashBoardStats(totalOrders, totalProducts, totalCategories, totalUsers);
    }
}
