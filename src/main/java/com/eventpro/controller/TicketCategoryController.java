package com.eventpro.controller;

import com.eventpro.dto.CategoryCreateRequest;
import com.eventpro.dto.CategoryResponse;
import com.eventpro.service.TicketCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    public TicketCategoryController(TicketCategoryService ticketCategoryService) {
        this.ticketCategoryService = ticketCategoryService;
    }

    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return ticketCategoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable Long id) {
        return ticketCategoryService.getCategoryById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return ticketCategoryService.createCategory(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        ticketCategoryService.deleteCategory(id);
    }
}

