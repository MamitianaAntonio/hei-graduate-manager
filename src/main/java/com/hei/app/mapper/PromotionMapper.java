package com.hei.app.mapper;

import com.hei.app.dto.promotion.PromotionRequest;
import com.hei.app.dto.promotion.PromotionResponse;
import com.hei.app.model.Promotion;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {
  public Promotion toEntity(PromotionRequest request) {
    Promotion promotion = new Promotion();
    promotion.setYear(request.year());
    return promotion;
  }

  public PromotionResponse toResponse(Promotion promotion) {
    return new PromotionResponse(promotion.getId(), promotion.getYear());
  }
}
