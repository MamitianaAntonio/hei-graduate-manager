package com.hei.app.controller;

import com.hei.app.dto.teacher.TeacherRequest;
import com.hei.app.dto.teacher.TeacherResponse;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.TeacherService;
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
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {
  private final TeacherService teacherService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<TeacherResponse> create(
      @RequestBody TeacherRequest request, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    TeacherResponse response = teacherService.create(request, currentUser);
    URI location = URI.create("/api/teachers/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/me")
  public TeacherResponse getMe(@AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    return teacherService.findByUserAccountId(currentUser.accountId(), currentUser);
  }

  @GetMapping("/{id}")
  public TeacherResponse getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    return teacherService.findById(id, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping
  public List<TeacherResponse> getAll(@AuthenticationPrincipal AppUserDetails appUser) {
    return teacherService.findAll(currentUserResolver.resolve(appUser.principal()));
  }

  @PutMapping("/{id}")
  public TeacherResponse update(
      @PathVariable UUID id,
      @RequestBody TeacherRequest request,
      @AuthenticationPrincipal AppUserDetails appUser) {
    return teacherService.update(id, request, currentUserResolver.resolve(appUser.principal()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    teacherService.delete(id, currentUserResolver.resolve(appUser.principal()));
  }
}
