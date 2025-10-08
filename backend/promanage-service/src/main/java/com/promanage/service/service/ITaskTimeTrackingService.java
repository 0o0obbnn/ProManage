package com.promanage.service.service;

import com.promanage.service.entity.TaskTimeTracking;

import java.util.List;

public interface ITaskTimeTrackingService {
    void logTime(TaskTimeTracking timeTracking);
    List<TaskTimeTracking> getTimeLogsForTask(Long taskId);
    Double getTotalHoursForTask(Long taskId);
}
