package com.hei.app.mapper;

import com.hei.app.dto.group.GroupRequest;
import com.hei.app.dto.group.GroupResponse;
import com.hei.app.model.Group;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {
  public Group toEntity(GroupRequest request) {
    Group group = new Group();

    group.setRef(request.ref());
    group.setTrack(request.track());

    return group;
  }

  public GroupResponse toResponse(Group group) {
    return new GroupResponse(group.getId(), group.getRef(), group.getTrack());
  }
}
