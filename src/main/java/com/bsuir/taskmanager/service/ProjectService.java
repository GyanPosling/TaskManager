package com.bsuir.taskmanager.service;

import com.bsuir.taskmanager.dto.request.ProjectRequest;
import com.bsuir.taskmanager.dto.response.ProjectResponse;
import com.bsuir.taskmanager.mapper.ProjectMapper;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.ProjectRepository;
import com.bsuir.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    public List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    public ProjectResponse findById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        User owner = getOwner(request.getOwnerId());
        Project project = projectMapper.fromRequest(request, owner);
        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + id));
        User owner = getOwner(request.getOwnerId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);
        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found: " + id);
        }
        projectRepository.deleteById(id);
    }

    private User getOwner(Long ownerId) {
        return userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + ownerId));
    }
}
