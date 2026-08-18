package com.hei.app.service;

import com.hei.app.dto.promotion.PromotionRequest;
import com.hei.app.dto.promotion.PromotionResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.PromotionMapper;
import com.hei.app.model.Promotion;
import com.hei.app.repository.PromotionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionService {
  private final PromotionRepository promotionRepository;
  private final PromotionMapper promotionMapper;

  public PromotionResponse create(PromotionRequest request) {
    if (promotionRepository.existsByYear(request.year())) {
      throw new DuplicateResourceException("Promotion already exists for year: " + request.year());
    }

    Promotion promotion = promotionMapper.toEntity(request);
    Promotion savedPromotion = promotionRepository.save(promotion);

    return promotionMapper.toResponse(savedPromotion);
  }

  public PromotionResponse findById(UUID id) {
    Promotion promotion =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

    return promotionMapper.toResponse(promotion);
  }

  public PromotionResponse findByYear(Integer year) {
    Promotion promotion =
        promotionRepository
            .findByYear(year)
            .orElseThrow(
                () -> new ResourceNotFoundException("Promotion not found for year: " + year));

    return promotionMapper.toResponse(promotion);
  }

  public List<PromotionResponse> findAll() {
    return promotionRepository.findAll().stream().map(promotionMapper::toResponse).toList();
  }

  public PromotionResponse update(UUID id, PromotionRequest request) {
    Promotion promotion =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

    promotion.setYear(request.year());

    Promotion updatedPromotion = promotionRepository.save(promotion);
    return promotionMapper.toResponse(updatedPromotion);
  }

  public void delete(UUID id) {
    if (!promotionRepository.existsById(id)) {
      throw new ResourceNotFoundException("Promotion not found with id: " + id);
    }

    promotionRepository.deleteById(id);
  }
}
