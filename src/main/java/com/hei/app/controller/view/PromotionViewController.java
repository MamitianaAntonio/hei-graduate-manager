package com.hei.app.controller.view;

import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.GraduateExportService;
import com.hei.app.service.PromotionService;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/promotions")
public class PromotionViewController {
  private final PromotionService promotionService;
  private final CurrentUserResolver currentUserResolver;
  private final GraduateExportService graduateExportService;

  public PromotionViewController(
      PromotionService promotionService,
      CurrentUserResolver currentUserResolver,
      GraduateExportService graduateExportService) {
    this.promotionService = promotionService;
    this.currentUserResolver = currentUserResolver;
    this.graduateExportService = graduateExportService;
  }

  @GetMapping
  public String promotions(Model model, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    model.addAttribute("promotions", promotionService.findAll(currentUser));
    return "promotions";
  }

  @GetMapping("/{id}")
  public String promotionDetails(
      @PathVariable UUID id, Model model, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    model.addAttribute("promotion", promotionService.findById(id, currentUser));
    model.addAttribute("graduates", graduateExportService.findGraduates(id, currentUser));
    return "promotion-details";
  }
}
