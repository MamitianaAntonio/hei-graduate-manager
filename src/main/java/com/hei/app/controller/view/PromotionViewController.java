package com.hei.app.controller.view;

import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.PromotionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/promotions")
public class PromotionViewController {
  private final PromotionService promotionService;
  private final CurrentUserResolver currentUserResolver;

  public PromotionViewController(
      PromotionService promotionService, CurrentUserResolver currentUserResolver) {
    this.promotionService = promotionService;
    this.currentUserResolver = currentUserResolver;
  }

  @GetMapping
  public String promotions(Model model, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    model.addAttribute("promotions", promotionService.findAll(currentUser));
    return "promotions";
  }
}
