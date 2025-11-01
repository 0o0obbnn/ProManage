package com.promanage.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.promanage.common.result.PageResult;
import com.promanage.service.entity.ProjectActivity;

public interface IProjectActivityService extends IService<ProjectActivity> {

  /**
   * 记录项目活动
   *
   * @param projectId 项目ID
   * @param userId 用户ID
   * @param activityType 活动类型
   * @param content 活动内容
   */
  void recordActivity(Long projectId, Long userId, String activityType, String content);

  /**
   * 分页获取项目活动
   *
   * @param projectId 项目ID
   * @param page 页码
   * @param pageSize 每页数量
   * @return 分页的活动列表
   */
  PageResult<ProjectActivity> getProjectActivities(Long projectId, Integer page, Integer pageSize);
}
