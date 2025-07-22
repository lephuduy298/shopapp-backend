package com.project.shopapp.controller;

import com.project.shopapp.dto.res.ResDashBoardStats;
import com.project.shopapp.services.DashBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/dash-board")
@RequiredArgsConstructor
public class DashBoardController {

    private final DashBoardService dashBoardService;

    @GetMapping("/stats")
    public ResDashBoardStats getDashboardStats() {
        return this.dashBoardService.getDashboardStats();
    }
}
