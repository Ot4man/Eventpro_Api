package com.eventpro.service;

import com.eventpro.dto.CategoryCreateRequest;
import com.eventpro.dto.CategoryResponse;
import com.eventpro.dto.TicketCategoryResponse;
import com.eventpro.entity.TicketCategory;
import com.eventpro.entity.TicketCategoriesPerEvent;
import com.eventpro.exception.ResourceNotFoundException;
import com.eventpro.repository.TicketCategoryRepository;
import com.eventpro.repository.TicketCategoriesPerEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketCategoriesPerEventRepository eventCategoryRepository;

    public TicketCategoryService(
            TicketCategoryRepository ticketCategoryRepository,
            TicketCategoriesPerEventRepository eventCategoryRepository
    ) {
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.eventCategoryRepository = eventCategoryRepository;
    }

    @Transactional
    public List<CategoryResponse> getAllCategories() {
        return ticketCategoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse getCategoryById(Long id) {
        TicketCategory category = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie de billet introuvable : " + id));
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        if (ticketCategoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new IllegalArgumentException("Une catégorie avec ce nom existe déjà");
        }

        TicketCategory category = TicketCategory.builder()
                .name(request.name().trim())
                .build();

        return toResponse(ticketCategoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        TicketCategory category = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + id));
        ticketCategoryRepository.delete(category);
    }

    @Transactional
    public TicketCategoryResponse updateEventCategory(
            Long eventCategoryId,
            BigDecimal unitPrice,
            int totalQuantity,
            int maxQuotaPerClient
    ) {
        TicketCategoriesPerEvent eventCat = eventCategoryRepository.findById(eventCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration de catégorie introuvable : " + eventCategoryId));

        if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) >= 0) {
            eventCat.setUnitPrice(unitPrice);
        }
        if (totalQuantity > 0) {
            eventCat.setTotalQuantity(totalQuantity);
        }
        if (maxQuotaPerClient > 0) {
            eventCat.setMaxQuotaPerClient(maxQuotaPerClient);
        }

        TicketCategoriesPerEvent saved = eventCategoryRepository.save(eventCat);

        return TicketCategoryResponse.builder()
                .id(saved.getId())
                .categoryId(saved.getCategory().getId())
                .name(saved.getCategory().getName())
                .unitPrice(saved.getUnitPrice())
                .totalQuantity(saved.getTotalQuantity())
                .maxQuotaPerClient(saved.getMaxQuotaPerClient())
                .build();
    }

    private CategoryResponse toResponse(TicketCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
