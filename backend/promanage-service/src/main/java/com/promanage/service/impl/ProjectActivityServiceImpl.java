package com.promanage.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.promanage.common.result.PageResult;
import com.promanage.service.IProjectActivityService;
import com.promanage.service.entity.ProjectActivity;
import com.promanage.service.mapper.ProjectActivityMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectActivityServiceImpl extends ServiceImpl<ProjectActivityMapper, ProjectActivity>
    implements IProjectActivityService {

  @Override
  public void recordActivity(Long projectId, Long userId, String activityType, String content) {
    ProjectActivity activity = new ProjectActivity();
    activity.setProjectId(projectId);
    activity.setUserId(userId);
    activity.setActivityType(activityType);
    activity.setContent(content);
    this.save(activity);
  }

  @Override
  public PageResult<ProjectActivity> getProjectActivities(
      Long projectId, Integer page, Integer pageSize) {
    Page<ProjectActivity> pageRequest = new Page<>(page, pageSize);
    LambdaQueryWrapper<ProjectActivity> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper
        .eq(ProjectActivity::getProjectId, projectId)
        .orderByDesc(ProjectActivity::getCreateTime);
    Page<ProjectActivity> pageResult = this.page(pageRequest, queryWrapper);
    return PageResult.of(
        pageResult.getRecords(),
        pageResult.getTotal(),
        (int) pageResult.getCurrent(),
        (int) pageResult.getSize());
  }
}
