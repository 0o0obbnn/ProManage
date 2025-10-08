package com.promanage.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.promanage.common.domain.PageResult;
import com.promanage.service.entity.*;
import com.promanage.service.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskCommentMapper taskCommentMapper;

    @Mock
    private TaskDependencyMapper taskDependencyMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TaskActivityMapper taskActivityMapper;

    @Mock
    private TaskAttachmentMapper taskAttachmentMapper;

    @Mock
    private TaskCheckItemMapper taskCheckItemMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTask() {
        // Given
        Task task = new Task();
        task.setProjectId(1L);
        task.setTitle("Test Task");
        task.setReporterId(1L);

        Project project = new Project();
        project.setId(1L);
        project.setDeleted(false);
        project.setOwnerId(1L);

        when(projectMapper.selectById(1L)).thenReturn(project);
        when(taskMapper.insert(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(1L);
            t.setCreateTime(LocalDateTime.now());
            return 1;
        });

        // When
        Long taskId = taskService.createTask(task);

        // Then
        assertNotNull(taskId);
        assertEquals(1L, taskId);
        verify(taskMapper, times(1)).insert(any(Task.class));
    }

    @Test
    void testListTaskActivities() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setDeleted(false);
        task.setReporterId(1L);
        task.setAssigneeId(1L);

        TaskActivity activity1 = new TaskActivity();
        activity1.setId(1L);
        activity1.setTaskId(1L);
        activity1.setActivityType("CREATE");

        TaskActivity activity2 = new TaskActivity();
        activity2.setId(2L);
        activity2.setTaskId(1L);
        activity2.setActivityType("UPDATE");

        IPage<TaskActivity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(activity1, activity2));
        page.setTotal(2L);

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskActivityMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> page);

        // When
        PageResult<TaskActivity> result = taskService.listTaskActivities(1L, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getList().size());
        verify(taskActivityMapper, times(1)).selectPage(any(), any());
    }

    @Test
    void testAddTaskActivity() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setDeleted(false);
        task.setReporterId(1L);
        task.setAssigneeId(1L);

        TaskActivity activity = new TaskActivity();
        activity.setTaskId(1L);
        activity.setActivityType("CREATE");
        activity.setContent("Task created");

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskActivityMapper.insert(any(TaskActivity.class))).thenAnswer(invocation -> {
            TaskActivity a = invocation.getArgument(0);
            a.setId(1L);
            return 1;
        });

        // When
        Long activityId = taskService.addTaskActivity(activity);

        // Then
        assertNotNull(activityId);
        assertEquals(1L, activityId);
        verify(taskActivityMapper, times(1)).insert(any(TaskActivity.class));
    }

    @Test
    void testListTaskAttachments() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setDeleted(false);
        task.setReporterId(1L);
        task.setAssigneeId(1L);

        TaskAttachment attachment1 = new TaskAttachment();
        attachment1.setId(1L);
        attachment1.setTaskId(1L);
        attachment1.setFileName("file1.txt");

        TaskAttachment attachment2 = new TaskAttachment();
        attachment2.setId(2L);
        attachment2.setTaskId(1L);
        attachment2.setFileName("file2.txt");

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskAttachmentMapper.selectList(any())).thenReturn(Arrays.asList(attachment1, attachment2));

        // When
        List<TaskAttachment> attachments = taskService.listTaskAttachments(1L);

        // Then
        assertNotNull(attachments);
        assertEquals(2, attachments.size());
        verify(taskAttachmentMapper, times(1)).selectList(any());
    }

    @Test
    void testAddTaskAttachment() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setDeleted(false);
        task.setReporterId(1L);
        task.setAssigneeId(1L);

        TaskAttachment attachment = new TaskAttachment();
        attachment.setTaskId(1L);
        attachment.setFileName("test.txt");
        attachment.setFileSize(1024L);

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskAttachmentMapper.insert(any(TaskAttachment.class))).thenAnswer(invocation -> {
            TaskAttachment a = invocation.getArgument(0);
            a.setId(1L);
            return 1;
        });

        // When
        Long attachmentId = taskService.addTaskAttachment(attachment);

        // Then
        assertNotNull(attachmentId);
        assertEquals(1L, attachmentId);
        verify(taskAttachmentMapper, times(1)).insert(any(TaskAttachment.class));
    }

    @Test
    void testDeleteTaskAttachment() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setDeleted(false);
        task.setReporterId(1L);
        task.setAssigneeId(1L);

        TaskAttachment attachment = new TaskAttachment();
        attachment.setId(1L);
        attachment.setTaskId(1L);
        attachment.setDeleted(false);

        when(taskAttachmentMapper.selectById(1L)).thenReturn(attachment);
        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskAttachmentMapper.updateById(any(TaskAttachment.class))).thenReturn(1);

        // When
        taskService.deleteTaskAttachment(1L, 1L);

        // Then
        verify(taskAttachmentMapper, times(1)).updateById(any(TaskAttachment.class));
    }

    @Test
    void testListTaskCheckItems() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setDeleted(false);
        task.setReporterId(1L);
        task.setAssigneeId(1L);

        TaskCheckItem checkItem1 = new TaskCheckItem();
        checkItem1.setId(1L);
        checkItem1.setTaskId(1L);
        checkItem1.setContent("Check item 1");

        TaskCheckItem checkItem2 = new TaskCheckItem();
        checkItem2.setId(2L);
        checkItem2.setTaskId(1L);
        checkItem2.setContent("Check item 2");

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskCheckItemMapper.selectList(any())).thenReturn(Arrays.asList(checkItem1, checkItem2));

        // When
        List<TaskCheckItem> checkItems = taskService.listTaskCheckItems(1L);

        // Then
        assertNotNull(checkItems);
        assertEquals(2, checkItems.size());
        verify(taskCheckItemMapper, times(1)).selectList(any());
    }

    @Test
    void testAddTaskCheckItem() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setDeleted(false);
        task.setReporterId(1L);
        task.setAssigneeId(1L);

        TaskCheckItem checkItem = new TaskCheckItem();
        checkItem.setTaskId(1L);
        checkItem.setContent("Check item");

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskCheckItemMapper.insert(any(TaskCheckItem.class))).thenAnswer(invocation -> {
            TaskCheckItem ci = invocation.getArgument(0);
            ci.setId(1L);
            return 1;
        });

        // When
        Long checkItemId = taskService.addTaskCheckItem(checkItem);

        // Then
        assertNotNull(checkItemId);
        assertEquals(1L, checkItemId);
        verify(taskCheckItemMapper, times(1)).insert(any(TaskCheckItem.class));
    }

    @Test
    void testUpdateTaskCheckItem() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setDeleted(false);
        task.setReporterId(1L);
        task.setAssigneeId(1L);

        TaskCheckItem existingCheckItem = new TaskCheckItem();
        existingCheckItem.setId(1L);
        existingCheckItem.setTaskId(1L);

        TaskCheckItem checkItem = new TaskCheckItem();
        checkItem.setId(1L);
        checkItem.setTaskId(1L);
        checkItem.setContent("Updated check item");
        checkItem.setUpdaterId(1L);

        when(taskCheckItemMapper.selectById(1L)).thenReturn(existingCheckItem);
        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskCheckItemMapper.updateById(any(TaskCheckItem.class))).thenReturn(1);

        // When
        taskService.updateTaskCheckItem(checkItem);

        // Then
        verify(taskCheckItemMapper, times(1)).updateById(any(TaskCheckItem.class));
    }

    @Test
    void testDeleteTaskCheckItem() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setDeleted(false);
        task.setReporterId(1L);
        task.setAssigneeId(1L);

        TaskCheckItem checkItem = new TaskCheckItem();
        checkItem.setId(1L);
        checkItem.setTaskId(1L);

        when(taskCheckItemMapper.selectById(1L)).thenReturn(checkItem);
        when(taskMapper.selectById(1L)).thenReturn(task);

        // When
        taskService.deleteTaskCheckItem(1L, 1L);

        // Then
        verify(taskCheckItemMapper, times(1)).deleteById(1L);
    }
}