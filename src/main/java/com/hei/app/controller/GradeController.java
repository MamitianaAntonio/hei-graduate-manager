package com.hei.app.controller;

import com.hei.app.dto.grade.GradeRequest;
import com.hei.app.dto.grade.GradeResponse;
import com.hei.app.dto.grade.GradeUpdateRequest;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.GradeService;
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
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {
  private final GradeService gradeService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<GradeResponse> create(
      @RequestBody GradeRequest request, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    GradeResponse response = gradeService.create(request, currentUser);
    URI location = URI.create("/api/grades/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public GradeResponse getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    return gradeService.findById(id, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping
  public List<GradeResponse> getAll(@AuthenticationPrincipal AppUserDetails appUser) {
    return gradeService.findAll(currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/student/{studentId}")
  public List<GradeResponse> getByStudentId(
      @PathVariable UUID studentId, @AuthenticationPrincipal AppUserDetails appUser) {
    return gradeService.findByStudentId(
        studentId, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/exam/{examId}")
  public List<GradeResponse> getByExamId(
      @PathVariable UUID examId, @AuthenticationPrincipal AppUserDetails appUser) {
    return gradeService.findByExamId(examId, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/student/{studentId}/exam/{examId}")
  public GradeResponse getByStudentAndExam(
      @PathVariable UUID studentId,
      @PathVariable UUID examId,
      @AuthenticationPrincipal AppUserDetails appUser) {
    return gradeService.findByStudentAndExam(
        studentId, examId, currentUserResolver.resolve(appUser.principal()));
  }

  @PutMapping("/{id}")
  public GradeResponse update(
      @PathVariable UUID id,
      @RequestBody GradeUpdateRequest request,
      @AuthenticationPrincipal AppUserDetails appUser) {
    return gradeService.update(id, request, currentUserResolver.resolve(appUser.principal()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    gradeService.delete(id, currentUserResolver.resolve(appUser.principal()));
  }
}
