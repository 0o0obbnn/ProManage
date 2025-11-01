package com.promanage.service.strategy.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.promanage.service.IProjectService;
import com.promanage.service.entity.Project;
import com.promanage.service.entity.Task;
import com.promanage.service.service.ITaskService;
import com.promanage.service.strategy.NotificationStrategy;

import org.springframework.dao.DataAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 任务通知策略 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskNotificationStrategy implements NotificationStrategy {

  private final ITaskService taskService;
  private final IProjectService projectService;

  @Override
  public List<Long> getRecipients(Long relatedId, String relatedType, Long operatorId) {
    try {
      Task task = taskService.getTaskById(relatedId);
      if (task == null) {
        return List.of();
      }

      return switch (relatedType) {
        case "TASK_CREATED", "TASK_UPDATED" -> {
          // 任务创建和更新通知给项目成员
          Project project = projectService.getById(task.getProjectId());
          if (project != null) {
            yield List.of(project.getOwnerId(), task.getAssigneeId()).stream()
                .filter(userId -> userId != null && !userId.equals(operatorId))
                .distinct()
                .toList();
          }
          yield List.of();
        }
        case "TASK_ASSIGNED" -> {
          // 任务分配通知给被分配者
          if (task.getAssigneeId() != null && !task.getAssigneeId().equals(operatorId)) {
            yield List.of(task.getAssigneeId());
          }
          yield List.of();
        }
        case "TASK_COMPLETED" -> {
          // 任务完成通知给项目所有者和创建者
          Project project = projectService.getById(task.getProjectId());
          if (project != null) {
            yield List.of(project.getOwnerId(), task.getCreatorId()).stream()
                .filter(userId -> userId != null && !userId.equals(operatorId))
                .distinct()
                .toList();
          }
          yield List.of();
        }
        case "TASK_OVERDUE" -> {
          // 任务逾期通知给被分配者和项目所有者
          Project project = projectService.getById(task.getProjectId());
          if (project != null) {
            yield List.of(task.getAssigneeId(), project.getOwnerId()).stream()
                .filter(userId -> userId != null && !userId.equals(operatorId))
                .distinct()
                .toList();
          }
          yield List.of();
        }
        default -> List.of();
      };
    } catch (DataAccessException e) {
      log.error("获取任务通知接收者失败, 任务ID: {}", relatedId, e);
      return List.of();
    }
  }

  @Override
  public String generateTitle(Long relatedId, String relatedType, Long operatorId) {
    try {
      Task task = taskService.getTaskById(relatedId);
      if (task == null) {
        return "任务通知";
      }

      return switch (relatedType) {
        case "TASK_CREATED" -> "新任务创建: " + task.getTitle();
        case "TASK_UPDATED" -> "任务更新: " + task.getTitle();
        case "TASK_ASSIGNED" -> "任务分配: " + task.getTitle();
        case "TASK_COMPLETED" -> "任务完成: " + task.getTitle();
        case "TASK_OVERDUE" -> "任务逾期: " + task.getTitle();
        default -> "任务通知: " + task.getTitle();
      };
    } catch (DataAccessException e) {
      log.error("生成任务通知标题失败, 任务ID: {}", relatedId, e);
      return "任务通知";
    }
  }

  @Override
  public String generateContent(Long relatedId, String relatedType, Long operatorId) {
    try {
      Task task = taskService.getTaskById(relatedId);
      if (task == null) {
        return "任务相关通知";
      }

      Project project = projectService.getById(task.getProjectId());
      String projectName = project != null ? project.getName() : "未知项目";

      return switch (relatedType) {
        case "TASK_CREATED" -> String.format(
            "项目 '%s' 中创建了新任务 '%s'，优先级: %s", projectName, task.getTitle(), task.getPriority());
        case "TASK_UPDATED" -> String.format("任务 '%s' 信息已更新", task.getTitle());
        case "TASK_ASSIGNED" -> String.format(
            "您被分配了新任务 '%s'，截止日期: %s", task.getTitle(), task.getDueDate());
        case "TASK_COMPLETED" -> String.format("任务 '%s' 已完成", task.getTitle());
        case "TASK_OVERDUE" -> String.format("任务 '%s' 已逾期，请及时处理", task.getTitle());
        default -> String.format("任务 '%s' 有新的更新", task.getTitle());
      };
    } catch (DataAccessException e) {
      log.error("生成任务通知内容失败, 任务ID: {}", relatedId, e);
      return "任务相关通知";
    }
  }
}
