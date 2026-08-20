package com.hei.app.controller;

import com.hei.app.dto.course.CourseRequest;
import com.hei.app.dto.course.CourseResponse;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.CourseService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
  private final CourseService courseService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<CourseResponse> create(
      @RequestBody CourseRequest request, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    CourseResponse response = courseService.create(request, currentUser);
    URI location = URI.create("/api/courses/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public CourseResponse getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    return courseService.findById(id, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/ref/{ref}")
  public CourseResponse getByRef(
      @PathVariable String ref, @AuthenticationPrincipal AppUserDetails appUser) {
    return courseService.findByRef(ref, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping
  public List<CourseResponse> getAll(@AuthenticationPrincipal AppUserDetails appUser) {
    return courseService.findAll(currentUserResolver.resolve(appUser.principal()));
  }

  @PutMapping("/{id}")
  public CourseResponse update(
      @PathVariable UUID id,
      @RequestBody CourseRequest request,
      @AuthenticationPrincipal AppUserDetails appUser) {
    return courseService.update(id, request, currentUserResolver.resolve(appUser.principal()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    courseService.delete(id, currentUserResolver.resolve(appUser.principal()));
  }
}
