package com.promanage.service.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.ProjectMember;
import com.promanage.service.IProjectService;
import com.promanage.service.mapper.ProjectMemberMapper;
import com.promanage.service.strategy.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目通知策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectNotificationStrategy implements NotificationStrategy {

    private final IProjectService projectService;
    private final ProjectMemberMapper projectMemberMapper;

    @Override
    public List<Long> getRecipients(Long relatedId, String relatedType, Long operatorId) {
        try {
            // 获取项目所有成员
            QueryWrapper<ProjectMember> wrapper = new QueryWrapper<>();
            wrapper.eq("project_id", relatedId)
                   .eq("deleted", false);
            List<ProjectMember> members = projectMemberMapper.selectList(wrapper);
            
            // 排除操作者自己
            return members.stream()
                .map(ProjectMember::getUserId)
                .filter(userId -> !userId.equals(operatorId))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取项目通知接收者失败, 项目ID: {}", relatedId, e);
            return List.of();
        }
    }

    @Override
    public String generateTitle(Long relatedId, String relatedType, Long operatorId) {
        try {
            Project project = projectService.getById(relatedId);
            if (project == null) {
                return "项目通知";
            }
            
            return switch (relatedType) {
                case "PROJECT_CREATED" -> "新项目创建: " + project.getName();
                case "PROJECT_UPDATED" -> "项目更新: " + project.getName();
                case "PROJECT_MEMBER_ADDED" -> "项目成员添加: " + project.getName();
                case "PROJECT_MEMBER_REMOVED" -> "项目成员移除: " + project.getName();
                default -> "项目通知: " + project.getName();
            };
        } catch (Exception e) {
            log.error("生成项目通知标题失败, 项目ID: {}", relatedId, e);
            return "项目通知";
        }
    }

    @Override
    public String generateContent(Long relatedId, String relatedType, Long operatorId) {
        try {
            Project project = projectService.getById(relatedId);
            if (project == null) {
                return "项目相关通知";
            }
            
            return switch (relatedType) {
                case "PROJECT_CREATED" -> String.format("项目 '%s' 已创建，项目描述: %s", 
                    project.getName(), project.getDescription());
                case "PROJECT_UPDATED" -> String.format("项目 '%s' 信息已更新", project.getName());
                case "PROJECT_MEMBER_ADDED" -> String.format("项目 '%s' 添加了新成员", project.getName());
                case "PROJECT_MEMBER_REMOVED" -> String.format("项目 '%s' 移除了成员", project.getName());
                default -> String.format("项目 '%s' 有新的更新", project.getName());
            };
        } catch (Exception e) {
            log.error("生成项目通知内容失败, 项目ID: {}", relatedId, e);
            return "项目相关通知";
        }
    }
}