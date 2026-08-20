package com.hei.app.controller;

import com.hei.app.dto.promotion.PromotionRequest;
import com.hei.app.dto.promotion.PromotionResponse;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.PromotionService;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {
  private final PromotionService promotionService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<PromotionResponse> create(
      @RequestBody PromotionRequest request, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    PromotionResponse response = promotionService.create(request, currentUser);
    URI location = URI.create("/api/promotions/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public PromotionResponse getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    return promotionService.findById(id, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/year/{year}")
  public PromotionResponse getByYear(
      @PathVariable Integer year, @AuthenticationPrincipal AppUserDetails appUser) {
    return promotionService.findByYear(year, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping
  public List<PromotionResponse> getAll(@AuthenticationPrincipal AppUserDetails appUser) {
    return promotionService.findAll(currentUserResolver.resolve(appUser.principal()));
  }

  @PutMapping("/{id}")
  public PromotionResponse update(
      @PathVariable UUID id,
      @RequestBody PromotionRequest request,
      @AuthenticationPrincipal AppUserDetails appUser) {
    return promotionService.update(id, request, currentUserResolver.resolve(appUser.principal()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    promotionService.delete(id, currentUserResolver.resolve(appUser.principal()));
  }
}
