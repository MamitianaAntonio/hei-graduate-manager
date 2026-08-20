package com.hei.app.controller;

import com.hei.app.dto.group.GroupRequest;
import com.hei.app.dto.group.GroupResponse;
import com.hei.app.security.AppUserDetails;
import com.hei.app.security.CurrentUser;
import com.hei.app.security.CurrentUserResolver;
import com.hei.app.service.GroupService;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
  private final GroupService groupService;
  private final CurrentUserResolver currentUserResolver;

  @PostMapping
  public ResponseEntity<GroupResponse> create(
      @RequestBody GroupRequest request, @AuthenticationPrincipal AppUserDetails appUser) {
    CurrentUser currentUser = currentUserResolver.resolve(appUser.principal());
    GroupResponse response = groupService.create(request, currentUser);
    URI location = URI.create("/api/groups/" + response.id());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public GroupResponse getById(
      @PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    return groupService.findById(id, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping("/ref/{ref}")
  public GroupResponse getByRef(
      @PathVariable String ref, @AuthenticationPrincipal AppUserDetails appUser) {
    return groupService.findByRef(ref, currentUserResolver.resolve(appUser.principal()));
  }

  @GetMapping
  public List<GroupResponse> getAll(@AuthenticationPrincipal AppUserDetails appUser) {
    return groupService.findAll(currentUserResolver.resolve(appUser.principal()));
  }

  @PutMapping("/{id}")
  public GroupResponse update(
      @PathVariable UUID id,
      @RequestBody GroupRequest request,
      @AuthenticationPrincipal AppUserDetails appUser) {
    return groupService.update(id, request, currentUserResolver.resolve(appUser.principal()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails appUser) {
    groupService.delete(id, currentUserResolver.resolve(appUser.principal()));
  }
}
