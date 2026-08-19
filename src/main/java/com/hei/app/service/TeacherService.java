package com.hei.app.service;

import com.hei.app.dto.teacher.TeacherRequest;
import com.hei.app.dto.teacher.TeacherResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.exceptions.UnauthorizedActionException;
import com.hei.app.mapper.TeacherMapper;
import com.hei.app.model.Role;
import com.hei.app.model.Teacher;
import com.hei.app.repository.TeacherRepository;
import com.hei.app.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {
  private final TeacherRepository teacherRepository;
  private final TeacherMapper teacherMapper;
  private final SecurityAsserts securityAsserts;

  @Transactional
  public TeacherResponse create(TeacherRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can create teachers");
    }

    Teacher teacher = teacherMapper.toEntity(request);
    Teacher savedTeacher = teacherRepository.save(teacher);

    return teacherMapper.toResponse(savedTeacher);
  }

  public TeacherResponse findById(UUID requestedId, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot access teacher profiles");
    }

    UUID id = requestedId;
    if (currentUser.role() == Role.TEACHER) {
      id = securityAsserts.requireTeacherId(currentUser);
    }

    UUID finalId = id;
    Teacher teacher =
        teacherRepository
            .findById(finalId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Teacher not found with id: " + finalId));

    return teacherMapper.toResponse(teacher);
  }

  public List<TeacherResponse> findAll(CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can list all teachers");
    }

    return teacherRepository.findAll().stream().map(teacherMapper::toResponse).toList();
  }

  public TeacherResponse findByUserAccountId(UUID requestedUserAccountId, CurrentUser currentUser) {
    if (currentUser.role() == Role.STUDENT) {
      throw new UnauthorizedActionException("Student cannot access teacher profiles");
    }

    UUID userAccountId = requestedUserAccountId;
    if (currentUser.role() == Role.TEACHER) {
      userAccountId = currentUser.accountId();
    }

    UUID finalUserAccountId = userAccountId;
    Teacher teacher =
        teacherRepository
            .findByUserAccountId(finalUserAccountId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Teacher not found with user account id: " + finalUserAccountId));

    return teacherMapper.toResponse(teacher);
  }

  @Transactional
  public TeacherResponse update(UUID id, TeacherRequest request, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can update teachers");
    }

    Teacher teacher =
        teacherRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

    teacher.setFirstName(request.firstName());
    teacher.setLastName(request.lastName());

    Teacher updatedTeacher = teacherRepository.save(teacher);
    return teacherMapper.toResponse(updatedTeacher);
  }

  @Transactional
  public void delete(UUID id, CurrentUser currentUser) {
    if (currentUser.role() != Role.ADMIN) {
      throw new UnauthorizedActionException("Only admin can delete teachers");
    }

    if (!teacherRepository.existsById(id)) {
      throw new ResourceNotFoundException("Teacher not found with id: " + id);
    }
    teacherRepository.deleteById(id);
  }
}
