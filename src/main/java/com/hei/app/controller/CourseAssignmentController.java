package com.hei.app.controller;

import com.hei.app.dto.assignment.CourseAssignmentRequest;
import com.hei.app.dto.assignment.CourseAssignmentResponse;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.CourseAssignmentService;
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
@RequestMapping("/api/course-assignments")
@RequiredArgsConstructor
public class CourseAssignmentController {
  private final CourseAssignmentService courseAssignmentService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<CourseAssignmentResponse> create(
      @RequestBody CourseAssignmentRequest request,
      @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    CourseAssignmentResponse response = courseAssignmentService.create(request, currentUser);
    URI location = URI.create("/api/course-assignments/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public CourseAssignmentResponse getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    return courseAssignmentService.findById(id, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping
  public List<CourseAssignmentResponse> getAll(@AuthenticationPrincipal AppUserDetails appUser) {
    return courseAssignmentService.findAll(currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/teacher/{teacherId}")
  public List<CourseAssignmentResponse> getByTeacherId(
      @PathVariable UUID teacherId, @AuthenticationPrincipal AppUserDetails appUser) {
    return courseAssignmentService.findByTeacherId(
        teacherId, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/course/{courseId}")
  public List<CourseAssignmentResponse> getByCourseId(
      @PathVariable UUID courseId, @AuthenticationPrincipal AppUserDetails appUser) {
    return courseAssignmentService.findByCourseId(
        courseId, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/group/{groupId}")
  public List<CourseAssignmentResponse> getByGroupId(
      @PathVariable UUID groupId, @AuthenticationPrincipal AppUserDetails appUser) {
    return courseAssignmentService.findByGroupId(
        groupId, currentUserResolver.resolve(appUser.principal()));
  }

  @PutMapping("/{id}")
  public CourseAssignmentResponse update(
      @PathVariable UUID id,
      @RequestBody CourseAssignmentRequest request,
      @AuthenticationPrincipal AppUserDetails appUser) {
    return courseAssignmentService.update(
        id, request, currentUserResolver.resolve(appUser.principal()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    courseAssignmentService.delete(id, currentUserResolver.resolve(appUser.principal()));
  }
}
