package com.hei.app.service;

import com.hei.app.dto.teacher.TeacherRequest;
import com.hei.app.dto.teacher.TeacherResponse;
import com.hei.app.exceptions.ResourceNotFoundException;
import com.hei.app.mapper.TeacherMapper;
import com.hei.app.model.Teacher;
import com.hei.app.repository.TeacherRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeacherService {
  private final TeacherRepository teacherRepository;
  private final TeacherMapper teacherMapper;

  public TeacherResponse create(TeacherRequest request) {
    Teacher teacher = teacherMapper.toEntity(request);
    Teacher savedTeacher = teacherRepository.save(teacher);

    return teacherMapper.toResponse(savedTeacher);
  }

  public TeacherResponse findById(UUID id) {
    Teacher teacher =
        teacherRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

    return teacherMapper.toResponse(teacher);
  }

  public List<TeacherResponse> findAll() {
    return teacherRepository.findAll().stream().map(teacherMapper::toResponse).toList();
  }

  public TeacherResponse findByUserAccountId(UUID userAccountId) {
    Teacher teacher =
        teacherRepository
            .findByUserAccountId(userAccountId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Teacher not found with user account id: " + userAccountId));

    return teacherMapper.toResponse(teacher);
  }

  public TeacherResponse update(UUID id, TeacherRequest request) {
    Teacher teacher =
        teacherRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

    teacher.setFirstName(request.firstName());
    teacher.setLastName(request.lastName());

    Teacher updatedTeacher = teacherRepository.save(teacher);
    return teacherMapper.toResponse(updatedTeacher);
  }

  public void delete(UUID id) {
    if (!teacherRepository.existsById(id)) {
      throw new ResourceNotFoundException("Teacher not found with id: " + id);
    }
    teacherRepository.deleteById(id);
  }
}
