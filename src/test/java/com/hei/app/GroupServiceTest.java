package com.hei.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hei.app.dto.group.GroupRequest;
import com.hei.app.dto.group.GroupResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.GroupMapper;
import com.hei.app.model.Group;
import com.hei.app.model.Role;
import com.hei.app.model.Track;
import com.hei.app.repository.GroupRepository;
import com.hei.app.security.CurrentUser;
import com.hei.app.service.GroupService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {
  @Mock private GroupRepository groupRepository;

  @Mock private GroupMapper groupMapper;

  @InjectMocks private GroupService groupService;

  @Test
  void create_shouldReturnGroup() {
    GroupRequest request = mock(GroupRequest.class);
    Group group = new Group();
    Group savedGroup = new Group();
    GroupResponse response = mock(GroupResponse.class);

    when(request.ref()).thenReturn("K1");
    when(groupRepository.existsByRef("K1")).thenReturn(false);
    when(groupMapper.toEntity(request)).thenReturn(group);
    when(groupRepository.save(group)).thenReturn(savedGroup);
    when(groupMapper.toResponse(savedGroup)).thenReturn(response);

    GroupResponse result = groupService.create(request, admin());

    assertEquals(response, result);
  }

  @Test
  void create_shouldThrowWhenRefAlreadyExists() {
    GroupRequest request = mock(GroupRequest.class);

    when(request.ref()).thenReturn("K1");
    when(groupRepository.existsByRef("K1")).thenReturn(true);

    assertThrows(DuplicateResourceException.class, () -> groupService.create(request, admin()));
  }

  @Test
  void teacher_cannotCreateGroup() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> groupService.create(mock(GroupRequest.class), teacher(UUID.randomUUID())));
  }

  @Test
  void findById_shouldReturnGroup() {
    UUID id = UUID.randomUUID();
    Group group = new Group();
    GroupResponse response = mock(GroupResponse.class);

    when(groupRepository.findById(id)).thenReturn(Optional.of(group));
    when(groupMapper.toResponse(group)).thenReturn(response);

    GroupResponse result = groupService.findById(id, admin());

    assertEquals(response, result);
  }

  @Test
  void findById_shouldThrowWhenGroupDoesNotExist() {
    UUID id = UUID.randomUUID();

    when(groupRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> groupService.findById(id, admin()));
  }

  @Test
  void findByRef_shouldReturnGroup() {
    String ref = "K1";
    Group group = new Group();
    GroupResponse response = mock(GroupResponse.class);

    when(groupRepository.findByRef(ref)).thenReturn(Optional.of(group));
    when(groupMapper.toResponse(group)).thenReturn(response);

    GroupResponse result = groupService.findByRef(ref, admin());

    assertEquals(response, result);
  }

  @Test
  void findByRef_shouldThrowWhenGroupDoesNotExist() {
    String ref = "K1";

    when(groupRepository.findByRef(ref)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> groupService.findByRef(ref, admin()));
  }

  @Test
  void findAll_shouldReturnGroups() {
    Group group1 = new Group();
    group1.setRef("K1");
    Group group2 = new Group();
    group2.setRef("K2");
    GroupResponse response1 = mock(GroupResponse.class);
    GroupResponse response2 = mock(GroupResponse.class);

    when(groupRepository.findAll()).thenReturn(List.of(group1, group2));
    when(groupMapper.toResponse(group1)).thenReturn(response1);
    when(groupMapper.toResponse(group2)).thenReturn(response2);

    List<GroupResponse> result = groupService.findAll(admin());

    assertEquals(List.of(response1, response2), result);
  }

  @Test
  void student_cannotListGroups() {
    assertThrows(
        UnauthorizedActionException.class, () -> groupService.findAll(student(UUID.randomUUID())));
  }

  @Test
  void teacher_cannotReadGroup() {
    UUID id = UUID.randomUUID();
    assertThrows(
        UnauthorizedActionException.class,
        () -> groupService.findById(id, teacher(UUID.randomUUID())));
  }

  @Test
  void teacher_cannotListGroups() {
    assertThrows(
        UnauthorizedActionException.class, () -> groupService.findAll(teacher(UUID.randomUUID())));
  }

  @Test
  void student_cannotReadGroup() {
    UUID id = UUID.randomUUID();
    assertThrows(
        UnauthorizedActionException.class,
        () -> groupService.findById(id, student(UUID.randomUUID())));
  }

  @Test
  void update_shouldReturnUpdatedGroup() {
    UUID id = UUID.randomUUID();
    GroupRequest request = mock(GroupRequest.class);
    Group group = new Group();
    Group updatedGroup = new Group();
    GroupResponse response = mock(GroupResponse.class);

    when(request.ref()).thenReturn("K2");
    when(request.track()).thenReturn(Track.EL);
    when(groupRepository.findById(id)).thenReturn(Optional.of(group));
    when(groupRepository.save(group)).thenReturn(updatedGroup);
    when(groupMapper.toResponse(updatedGroup)).thenReturn(response);

    GroupResponse result = groupService.update(id, request, admin());

    assertEquals(response, result);
  }

  @Test
  void update_shouldThrowWhenGroupDoesNotExist() {
    UUID id = UUID.randomUUID();
    GroupRequest request = mock(GroupRequest.class);

    when(groupRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> groupService.update(id, request, admin()));
  }

  @Test
  void teacher_cannotUpdateGroup() {
    assertThrows(
        UnauthorizedActionException.class,
        () ->
            groupService.update(
                UUID.randomUUID(), mock(GroupRequest.class), teacher(UUID.randomUUID())));
  }

  @Test
  void delete_shouldDeleteGroup() {
    UUID id = UUID.randomUUID();
    when(groupRepository.existsById(id)).thenReturn(true);

    groupService.delete(id, admin());

    verify(groupRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowWhenGroupDoesNotExist() {
    UUID id = UUID.randomUUID();
    when(groupRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> groupService.delete(id, admin()));
  }

  @Test
  void student_cannotDeleteGroup() {
    assertThrows(
        UnauthorizedActionException.class,
        () -> groupService.delete(UUID.randomUUID(), student(UUID.randomUUID())));
  }

  private CurrentUser admin() {
    return new CurrentUser(UUID.randomUUID(), Role.ADMIN, null, null);
  }

  private CurrentUser student(UUID studentId) {
    return new CurrentUser(UUID.randomUUID(), Role.STUDENT, studentId, null);
  }

  private CurrentUser teacher(UUID teacherId) {
    return new CurrentUser(UUID.randomUUID(), Role.TEACHER, null, teacherId);
  }
}
