package com.hei.app.service;

import com.hei.app.dto.group.GroupRequest;
import com.hei.app.dto.group.GroupResponse;
import com.hei.app.exceptions.DuplicateResourceException;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.GroupMapper;
import com.hei.app.model.Group;
import com.hei.app.model.Role;
import com.hei.app.repository.GroupRepository;
import com.hei.app.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {
  private final GroupRepository groupRepository;
  private final GroupMapper groupMapper;

  @Transactional
  public GroupResponse create(GroupRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can create groups");
    }

    if (groupRepository.existsByRef(request.ref())) {
      throw new DuplicateResourceException("Group already exists with ref: " + request.ref());
    }

    Group group = groupMapper.toEntity(request);
    Group savedGroup = groupRepository.save(group);

    return groupMapper.toResponse(savedGroup);
  }

  public GroupResponse findById(UUID id, CurrentUser currentUser) {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));

    return groupMapper.toResponse(group);
  }

  public GroupResponse findByRef(String ref, CurrentUser currentUser) {
    Group group =
        groupRepository
            .findByRef(ref)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with ref: " + ref));

    return groupMapper.toResponse(group);
  }

  public List<GroupResponse> findAll(CurrentUser currentUser) {
    return groupRepository.findAll().stream().map(groupMapper::toResponse).toList();
  }

  @Transactional
  public GroupResponse update(UUID id, GroupRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can update groups");
    }

    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));

    group.setRef(request.ref());
    group.setTrack(request.track());

    Group updatedGroup = groupRepository.save(group);
    return groupMapper.toResponse(updatedGroup);
  }

  @Transactional
  public void delete(UUID id, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can delete groups");
    }

    if (!groupRepository.existsById(id)) {
      throw new ResourceNotFoundException("Group not found with id: " + id);
    }

    groupRepository.deleteById(id);
  }
}
