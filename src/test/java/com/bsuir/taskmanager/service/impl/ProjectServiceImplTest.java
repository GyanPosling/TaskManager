package com.bsuir.taskmanager.service.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bsuir.taskmanager.cache.TaskSearchCache;
import com.bsuir.taskmanager.exception.ProjectNotFoundException;
import com.bsuir.taskmanager.exception.UserNotFoundException;
import com.bsuir.taskmanager.mapper.ProjectMapper;
import com.bsuir.taskmanager.model.dto.request.ProjectRequest;
import com.bsuir.taskmanager.model.dto.response.ProjectResponse;
import com.bsuir.taskmanager.model.entity.Project;
import com.bsuir.taskmanager.model.entity.User;
import com.bsuir.taskmanager.repository.ProjectRepository;
import com.bsuir.taskmanager.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private TaskSearchCache taskSearchCache;

    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectServiceImpl(
                projectRepository,
                userRepository,
                projectMapper,
                taskSearchCache
        );
    }

    @Test
    void findAllShouldMapAllProjects() {
        Project firstProject = project(1L, "Release");
        Project secondProject = project(2L, "Roadmap");
        ProjectResponse firstResponse = new ProjectResponse(1L, "Release", "Release planning", 2L);
        ProjectResponse secondResponse = new ProjectResponse(2L, "Roadmap", "Roadmap planning", 3L);

        when(projectRepository.findAll()).thenReturn(List.of(firstProject, secondProject));
        when(projectMapper.toResponse(firstProject)).thenReturn(firstResponse);
        when(projectMapper.toResponse(secondProject)).thenReturn(secondResponse);

        List<ProjectResponse> result = projectService.findAll();

        assertSame(firstResponse, result.get(0));
        assertSame(secondResponse, result.get(1));
    }

    @Test
    void findByIdShouldReturnMappedProject() {
        Project project = project(11L, "Release");
        ProjectResponse response = new ProjectResponse(11L, "Release", "Release planning", 2L);

        when(projectRepository.findById(11L)).thenReturn(java.util.Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(response);

        ProjectResponse result = projectService.findById(11L);

        assertSame(response, result);
    }

    @Test
    void createShouldSaveProjectAndClearCache() {
        ProjectRequest request = new ProjectRequest("Release", "Release planning", 2L);
        User owner = user(2L);
        Project project = project(10L, "Release");
        ProjectResponse response = new ProjectResponse(10L, "Release", "Release planning", 2L);

        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(owner));
        when(projectMapper.fromRequest(request, owner)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(response);

        ProjectResponse result = projectService.create(request);

        assertSame(response, result);
        verify(taskSearchCache).clear();
    }

    @Test
    void createShouldThrowWhenOwnerMissing() {
        ProjectRequest request = new ProjectRequest("Release", "Release planning", 2L);

        when(userRepository.findById(2L)).thenReturn(java.util.Optional.empty());

        assertThrows(UserNotFoundException.class, () -> projectService.create(request));
    }

    @Test
    void findByIdShouldThrowWhenProjectMissing() {
        when(projectRepository.findById(11L)).thenReturn(java.util.Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.findById(11L));
    }

    @Test
    void updateShouldSaveProjectAndClearCache() {
        ProjectRequest request = new ProjectRequest("Release", "Updated description", 2L);
        Project project = project(7L, "Old release");
        User owner = user(2L);
        ProjectResponse response = new ProjectResponse(7L, "Release", "Updated description", 2L);

        when(projectRepository.findById(7L)).thenReturn(java.util.Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(owner));
        when(projectRepository.save(project)).thenReturn(project);
        when(projectMapper.toResponse(project)).thenReturn(response);

        ProjectResponse result = projectService.update(7L, request);

        assertSame(response, result);
        assertSame(owner, project.getOwner());
        verify(taskSearchCache).clear();
    }

    @Test
    void updateShouldThrowWhenProjectMissing() {
        ProjectRequest request = new ProjectRequest("Release", "Updated description", 2L);

        when(projectRepository.findById(7L)).thenReturn(java.util.Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.update(7L, request));
    }

    @Test
    void deleteShouldRemoveProjectAndClearCache() {
        when(projectRepository.existsById(7L)).thenReturn(true);

        projectService.delete(7L);

        verify(projectRepository).deleteById(7L);
        verify(taskSearchCache).clear();
    }

    @Test
    void deleteShouldThrowWhenProjectMissing() {
        when(projectRepository.existsById(7L)).thenReturn(false);

        assertThrows(ProjectNotFoundException.class, () -> projectService.delete(7L));
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Project project(Long id, String name) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        return project;
    }
}
