package com.hei.app.controller;

import com.hei.app.dto.exam.ExamRequest;
import com.hei.app.dto.exam.ExamResponse;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.ExamService;
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
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {
  private final ExamService examService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<ExamResponse> create(
      @RequestBody ExamRequest request, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    ExamResponse response = examService.create(request, currentUser);
    URI location = URI.create("/api/exams/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public ExamResponse getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    return examService.findById(id, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping
  public List<ExamResponse> getAll(@AuthenticationPrincipal AppUserDetails appUser) {
    return examService.findAll(currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/course/{courseId}")
  public List<ExamResponse> getByCourseId(
      @PathVariable UUID courseId, @AuthenticationPrincipal AppUserDetails appUser) {
    return examService.findByCourseId(courseId, currentUserResolver.resolve(appUser.principal()));
  }

  @PutMapping("/{id}")
  public ExamResponse update(
      @PathVariable UUID id,
      @RequestBody ExamRequest request,
      @AuthenticationPrincipal AppUserDetails appUser) {
    return examService.update(id, request, currentUserResolver.resolve(appUser.principal()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    examService.delete(id, currentUserResolver.resolve(appUser.principal()));
  }
}
