package com.promanage.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.promanage.dto.CreateProjectRequestDTO;
import com.promanage.dto.ProjectDTO;
import com.promanage.dto.UpdateProjectRequestDTO;
import com.promanage.service.entity.Project;

@Mapper(componentModel = "spring")
public interface ProjectDtoMapper {

  @Mapping(target = "ownerName", ignore = true)
  @Mapping(source = "createTime", target = "createdAt")
  @Mapping(source = "updateTime", target = "updatedAt")
  @Mapping(
      target = "archived",
      expression = "java(project.getStatus() != null && project.getStatus() == 3)")
  ProjectDTO toDto(Project project);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createTime", ignore = true)
  @Mapping(target = "updateTime", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deletedBy", ignore = true)
  @Mapping(target = "creatorId", ignore = true)
  @Mapping(target = "updaterId", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "coverImage", ignore = true)
  void updateEntity(UpdateProjectRequestDTO request, @MappingTarget Project project);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createTime", ignore = true)
  @Mapping(target = "updateTime", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deletedBy", ignore = true)
  @Mapping(target = "creatorId", ignore = true)
  @Mapping(target = "updaterId", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "progress", ignore = true)
  @Mapping(target = "coverImage", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  Project toEntity(CreateProjectRequestDTO request);

  /**
   * Temporary stub method to fix compilation.
   * TODO: Refactor ProjectQueryStrategy to use projectMapper.selectById() + toDto()
   * instead of calling this method on the DTO mapper.
   *
   * @param projectId Project ID
   * @return ProjectDTO (currently throws UnsupportedOperationException)
   * @throws UnsupportedOperationException Always thrown - method needs proper implementation
   */
  default ProjectDTO selectById(Long projectId) {
    throw new UnsupportedOperationException(
        "selectById should be called on ProjectMapper (MyBatis), not ProjectDtoMapper (MapStruct). "
        + "Use: Project project = projectMapper.selectById(id); return toDto(project);");
  }
}
