package com.hei.app.service;

import com.hei.app.dto.promotion.PromotionRequest;
import com.hei.app.dto.promotion.PromotionResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.PromotionMapper;
import com.hei.app.model.Promotion;
import com.hei.app.model.Role;
import com.hei.app.repository.PromotionRepository;
import com.hei.app.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {
  private final PromotionRepository promotionRepository;
  private final PromotionMapper promotionMapper;

  @Transactional
  public PromotionResponse create(PromotionRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can create promotions");
    }

    if (promotionRepository.existsByYear(request.year())) {
      throw new DuplicateResourceException("Promotion already exists for year: " + request.year());
    }

    Promotion promotion = promotionMapper.toEntity(request);
    Promotion savedPromotion = promotionRepository.save(promotion);

    return promotionMapper.toResponse(savedPromotion);
  }

  public PromotionResponse findById(UUID id, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can read promotions");
    }

    Promotion promotion =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

    return promotionMapper.toResponse(promotion);
  }

  public PromotionResponse findByYear(Integer year, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can read promotions");
    }

    Promotion promotion =
        promotionRepository
            .findByYear(year)
            .orElseThrow(
                () -> new ResourceNotFoundException("Promotion not found for year: " + year));

    return promotionMapper.toResponse(promotion);
  }

  public List<PromotionResponse> findAll(CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can read promotions");
    }

    return promotionRepository.findAll().stream().map(promotionMapper::toResponse).toList();
  }

  @Transactional
  public PromotionResponse update(UUID id, PromotionRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can update promotions");
    }

    Promotion promotion =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

    promotion.setYear(request.year());

    Promotion updatedPromotion = promotionRepository.save(promotion);
    return promotionMapper.toResponse(updatedPromotion);
  }

  @Transactional
  public void delete(UUID id, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can delete promotions");
    }

    if (!promotionRepository.existsById(id)) {
      throw new ResourceNotFoundException("Promotion not found with id: " + id);
    }

    promotionRepository.deleteById(id);
  }
}
