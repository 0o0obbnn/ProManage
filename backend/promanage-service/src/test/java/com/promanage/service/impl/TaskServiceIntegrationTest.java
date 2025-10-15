package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.promanage.common.result.PageResult;
import com.promanage.service.entity.*;
import com.promanage.service.mapper.TaskActivityMapper;
import com.promanage.service.mapper.TaskAttachmentMapper;
import com.promanage.service.mapper.TaskCheckItemMapper;
import com.promanage.service.service.ITaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class TaskServiceIntegrationTest {

    @Autowired
    private ITaskService taskService;

    @Autowired
    private TaskActivityMapper taskActivityMapper;

    @Autowired
    private TaskAttachmentMapper taskAttachmentMapper;

    @Autowired
    private TaskCheckItemMapper taskCheckItemMapper;

    @Test
    void testTaskActivityOperations() {
        // Create a task first
        Task task = new Task();
        task.setProjectId(1L);
        task.setTitle("Integration Test Task");
        task.setDescription("Task for testing activity operations");
        task.setReporterId(1L);
        task.setAssigneeId(1L);
        Long taskId = taskService.createTask(task);

        // Add activity
        TaskActivity activity = new TaskActivity();
        activity.setTaskId(taskId);
        activity.setProjectId(1L);
        activity.setUserId(1L);
        activity.setActivityType("CREATE");
        activity.setContent("Task created");
        Long activityId = taskService.addTaskActivity(activity);

        // List activities
        PageResult<TaskActivity> activities = taskService.listTaskActivities(taskId, 1, 10);
        assertNotNull(activities);
        assertEquals(1, activities.getList().size());
        assertEquals("CREATE", activities.getList().get(0).getActivityType());

        // Verify in database
        TaskActivity savedActivity = taskActivityMapper.selectById(activityId);
        assertNotNull(savedActivity);
        assertEquals("CREATE", savedActivity.getActivityType());
        assertEquals("Task created", savedActivity.getContent());
    }

    @Test
    void testTaskAttachmentOperations() {
        // Create a task first
        Task task = new Task();
        task.setProjectId(1L);
        task.setTitle("Integration Test Task for Attachments");
        task.setDescription("Task for testing attachment operations");
        task.setReporterId(1L);
        task.setAssigneeId(1L);
        Long taskId = taskService.createTask(task);

        // Add attachment
        TaskAttachment attachment = new TaskAttachment();
        attachment.setTaskId(taskId);
        attachment.setProjectId(1L);
        attachment.setFileName("test.txt");
        attachment.setFilePath("/uploads/test.txt");
        attachment.setFileSize(1024L);
        attachment.setMimeType("text/plain");
        attachment.setUploaderId(1L);
        attachment.setIsImage(false);
        Long attachmentId = taskService.addTaskAttachment(attachment);

        // List attachments
        List<TaskAttachment> attachments = taskService.listTaskAttachments(taskId);
        assertNotNull(attachments);
        assertEquals(1, attachments.size());
        assertEquals("test.txt", attachments.get(0).getFileName());

        // Delete attachment
        taskService.deleteTaskAttachment(attachmentId, 1L);

        // Verify in database
        TaskAttachment savedAttachment = taskAttachmentMapper.selectById(attachmentId);
        assertNotNull(savedAttachment);
        assertTrue(savedAttachment.getDeleted());
    }

    @Test
    void testTaskCheckItemOperations() {
        // Create a task first
        Task task = new Task();
        task.setProjectId(1L);
        task.setTitle("Integration Test Task for Check Items");
        task.setDescription("Task for testing check item operations");
        task.setReporterId(1L);
        task.setAssigneeId(1L);
        Long taskId = taskService.createTask(task);

        // Add check item
        TaskCheckItem checkItem = new TaskCheckItem();
        checkItem.setTaskId(taskId);
        checkItem.setProjectId(1L);
        checkItem.setContent("Test check item");
        checkItem.setIsCompleted(false);
        checkItem.setSortOrder(1);
        Long checkItemId = taskService.addTaskCheckItem(checkItem);

        // List check items
        List<TaskCheckItem> checkItems = taskService.listTaskCheckItems(taskId);
        assertNotNull(checkItems);
        assertEquals(1, checkItems.size());
        assertEquals("Test check item", checkItems.get(0).getContent());
        assertFalse(checkItems.get(0).getIsCompleted());

        // Update check item
        checkItem.setId(checkItemId);
        checkItem.setIsCompleted(true);
        checkItem.setCompletedById(1L);
        checkItem.setCompletedTime(LocalDateTime.now());
        checkItem.setUpdaterId(1L);
        taskService.updateTaskCheckItem(checkItem);

        // Verify update
        List<TaskCheckItem> updatedCheckItems = taskService.listTaskCheckItems(taskId);
        assertTrue(updatedCheckItems.get(0).getIsCompleted());

        // Delete check item
        taskService.deleteTaskCheckItem(checkItemId, 1L);

        // Verify deletion
        LambdaQueryWrapper<TaskCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCheckItem::getId, checkItemId);
        TaskCheckItem deletedCheckItem = taskCheckItemMapper.selectOne(wrapper);
        assertNull(deletedCheckItem);
    }
}

