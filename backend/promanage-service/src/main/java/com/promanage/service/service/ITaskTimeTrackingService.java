package com.promanage.service.service;

import java.util.List;

import com.promanage.service.entity.TaskTimeTracking;

public interface ITaskTimeTrackingService {
  void logTime(TaskTimeTracking timeTracking);

  List<TaskTimeTracking> getTimeLogsForTask(Long taskId);

  Double getTotalHoursForTask(Long taskId);
}
