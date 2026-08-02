package com.expense_splitter.expense_splitter.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/protected")
    public String protectedRoute() {
        return "You are authenticated! This is protected data.";
    }
}