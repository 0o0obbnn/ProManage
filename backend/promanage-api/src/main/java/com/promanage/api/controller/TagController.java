package com.promanage.api.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.promanage.common.domain.Result;
import com.promanage.service.entity.Tag;
import com.promanage.service.service.ITagService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "标签管理相关接口")
public class TagController {

  private final ITagService tagService;

  @PostMapping
  @Operation(summary = "创建标签")
  @PreAuthorize("hasAuthority('tag:manage')")
  public Result<Long> create(@RequestBody Tag tag) {
    Long id = tagService.createTag(tag);
    return Result.success(id);
  }

  @PutMapping("/{id}")
  @Operation(summary = "更新标签")
  @PreAuthorize("hasAuthority('tag:manage')")
  public Result<Void> update(@PathVariable Long id, @RequestBody Tag tag) {
    tagService.updateTag(id, tag);
    return Result.success();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "删除标签")
  @PreAuthorize("hasAuthority('tag:manage')")
  public Result<Void> delete(@PathVariable Long id) {
    tagService.deleteTag(id);
    return Result.success();
  }

  @GetMapping("/{id}")
  @Operation(summary = "获取标签详情")
  @PreAuthorize("hasAuthority('tag:view')")
  public Result<Tag> getById(@PathVariable Long id) {
    return Result.success(tagService.getTagById(id));
  }

  @GetMapping
  @Operation(summary = "标签列表/查询")
  @PreAuthorize("hasAuthority('tag:view')")
  public Result<List<Tag>> list(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long projectId,
      @RequestParam(required = false, defaultValue = "100") Integer limit) {
    if (name != null && !name.isBlank()) {
      Tag tag = tagService.getTagByName(name, projectId);
      return Result.success(tag == null ? List.of() : List.of(tag));
    }
    if (limit == null || limit <= 0) {
      limit = 100;
    }
    return Result.success(tagService.getPopularTags(projectId, limit));
  }
}
