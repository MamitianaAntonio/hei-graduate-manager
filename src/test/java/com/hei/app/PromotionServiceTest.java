package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.hei.app.service.PromotionService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {
  @Mock private PromotionRepository promotionRepository;

  @Mock private PromotionMapper promotionMapper;

  @InjectMocks private PromotionService promotionService;

  @Test
  void create_shouldReturnPromotion() {
    PromotionRequest request = mock(PromotionRequest.class);
    Promotion promotion = new Promotion();
    Promotion savedPromotion = new Promotion();
    PromotionResponse response = mock(PromotionResponse.class);

    when(request.year()).thenReturn(2026);
    when(promotionRepository.existsByYear(2026)).thenReturn(false);
    when(promotionMapper.toEntity(request)).thenReturn(promotion);
    when(promotionRepository.save(promotion)).thenReturn(savedPromotion);
    when(promotionMapper.toResponse(savedPromotion)).thenReturn(response);

    PromotionResponse result = promotionService.create(request, admin());

    assertEquals(response, result);
  }

  @Test
  void create_shouldThrowWhenYearAlreadyExists() {
    PromotionRequest request = mock(PromotionRequest.class);

    when(request.year()).thenReturn(2026);
    when(promotionRepository.existsByYear(2026)).thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> promotionService.create(request, admin()));
  }

  @Test
  void teacher_cannotCreatePromotion() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> promotionService.create(mock(PromotionRequest.class), teacher(UUID.randomUUID())));
  }

  @Test
  void findById_shouldReturnPromotion() {
    UUID id = UUID.randomUUID();
    Promotion promotion = new Promotion();
    PromotionResponse response = mock(PromotionResponse.class);

    when(promotionRepository.findById(id)).thenReturn(Optional.of(promotion));
    when(promotionMapper.toResponse(promotion)).thenReturn(response);

    PromotionResponse result = promotionService.findById(id, admin());

    assertEquals(response, result);
  }

  @Test
  void findById_shouldThrowWhenPromotionDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(promotionRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> promotionService.findById(id, admin()));
  }

  @Test
  void findByYear_shouldReturnPromotion() {
    Integer year = 2026;
    Promotion promotion = new Promotion();
    PromotionResponse response = mock(PromotionResponse.class);

    when(promotionRepository.findByYear(year)).thenReturn(Optional.of(promotion));
    when(promotionMapper.toResponse(promotion)).thenReturn(response);

    PromotionResponse result = promotionService.findByYear(year, admin());

    assertEquals(response, result);
  }

  @Test
  void findByYear_shouldThrowWhenPromotionDoesNotExist() {
    Integer year = 2026;

    when(promotionRepository.findByYear(year)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> promotionService.findByYear(year, admin()));
  }

  @Test
  void findAll_shouldReturnPromotions() {
    Promotion promotion1 = new Promotion();
    promotion1.setYear(2025);
    Promotion promotion2 = new Promotion();
    promotion2.setYear(2026);
    PromotionResponse response1 = mock(PromotionResponse.class);
    PromotionResponse response2 = mock(PromotionResponse.class);

    when(promotionRepository.findAll()).thenReturn(List.of(promotion1, promotion2));
    when(promotionMapper.toResponse(promotion1)).thenReturn(response1);
    when(promotionMapper.toResponse(promotion2)).thenReturn(response2);

    List<PromotionResponse> result = promotionService.findAll(admin());

    assertEquals(List.of(response1, response2), result);
  }

  @Test
  void student_canListPromotions() {
    Promotion promotion = new Promotion();
    promotion.setYear(2026);
    PromotionResponse response = mock(PromotionResponse.class);

    when(promotionRepository.findAll()).thenReturn(List.of(promotion));
    when(promotionMapper.toResponse(promotion)).thenReturn(response);

    List<PromotionResponse> result = promotionService.findAll(student(UUID.randomUUID()));

    assertEquals(List.of(response), result);
  }

  @Test
  void update_shouldReturnUpdatedPromotion() {
    UUID id = UUID.randomUUID();
    PromotionRequest request = mock(PromotionRequest.class);
    Promotion promotion = new Promotion();
    Promotion updatedPromotion = new Promotion();
    PromotionResponse response = mock(PromotionResponse.class);

    when(request.year()).thenReturn(2027);
    when(promotionRepository.findById(id)).thenReturn(Optional.of(promotion));
    when(promotionRepository.save(promotion)).thenReturn(updatedPromotion);
    when(promotionMapper.toResponse(updatedPromotion)).thenReturn(response);

    PromotionResponse result = promotionService.update(id, request, admin());

    assertEquals(response, result);
  }

  @Test
  void update_shouldThrowWhenPromotionDoesNotExist() {
    UUID id = UUID.randomUUID();
    PromotionRequest request = mock(PromotionRequest.class);

    when(promotionRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> promotionService.update(id, request, admin()));
  }

  @Test
  void teacher_cannotUpdatePromotion() {
    assertThrows(
        UnauthorizedActionException.class,
        () ->
            promotionService.update(
                UUID.randomUUID(), mock(PromotionRequest.class), teacher(UUID.randomUUID())));
  }

  @Test
  void delete_shouldDeletePromotion() {
    UUID id = UUID.randomUUID();

    when(promotionRepository.existsById(id)).thenReturn(true);

    promotionService.delete(id, admin());

    verify(promotionRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenPromotionDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(promotionRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> promotionService.delete(id, admin()));
  }

  @Test
  void student_cannotDeletePromotion() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> promotionService.delete(UUID.randomUUID(), student(UUID.randomUUID())));
  }

  private CurrentUser admin() {
    return new CurrentUser(UUID.randomUUID(), Role.ADMIN, null, null);
  }

  private CurrentUser student(UUID studentId) {
    return new CurrentUser(UUID.randomUUID(), Role.STUDENT, studentId, null);
  }

  private CurrentUser teacher(UUID teacherId) {
    return new CurrentUser(UUID.randomUUID(), Role.TEACHER, null, teacherId);
  }
}
