package com.hei.app.controller;

import com.hei.app.dto.transcript.TranscriptResponse;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.TranscriptService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transcripts")
@RequiredArgsConstructor
public class TranscriptController {
  private final TranscriptService transcriptService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping("/me")
  public ResponseEntity<TranscriptResponse> requestOwn(
      @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    return ResponseEntity.ok(transcriptService.requestOwn(currentUser));
  }

  @PostMapping
  public ResponseEntity<TranscriptResponse> requestForStudent(
      @RequestParam UUID studentId, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    return ResponseEntity.ok(transcriptService.requestForStudent(studentId, currentUser));
  }
}
