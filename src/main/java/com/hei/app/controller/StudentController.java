package com.hei.app.controller;

import com.hei.app.dto.student.StudentRequest;
import com.hei.app.dto.student.StudentResponse;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.StudentService;
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
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
  private final StudentService studentService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<StudentResponse> create(
      @RequestBody StudentRequest request, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    StudentResponse response = studentService.create(request, currentUser);
    URI location = URI.create("/api/students/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/me")
  public StudentResponse getMe(@AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    return studentService.findByUserAccountId(currentUser.accountId(), currentUser);
  }

  @GetMapping("/{id}")
  public StudentResponse getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    return studentService.findById(id, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/std/{std}")
  public StudentResponse getByStd(
      @PathVariable String std, @AuthenticationPrincipal AppUserDetails appUser) {
    return studentService.findByStd(std, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping
  public List<StudentResponse> getAll(@AuthenticationPrincipal AppUserDetails appUser) {
    return studentService.findAll(currentUserResolver.resolve(appUser.principal()));
  }

  @PutMapping("/{id}")
  public StudentResponse update(
      @PathVariable UUID id,
      @RequestBody StudentRequest request,
      @AuthenticationPrincipal AppUserDetails appUser) {
    return studentService.update(id, request, currentUserResolver.resolve(appUser.principal()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    studentService.delete(id, currentUserResolver.resolve(appUser.principal()));
  }
}
