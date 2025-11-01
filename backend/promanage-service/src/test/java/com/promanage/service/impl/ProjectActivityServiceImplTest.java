package com.promanage.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.promanage.common.result.PageResult;
import com.promanage.service.entity.ProjectActivity;
import com.promanage.service.mapper.ProjectActivityMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectActivityServiceImplTest {

  @Mock private ProjectActivityMapper projectActivityMapper;

  private ProjectActivityServiceImpl projectActivityService;

  @BeforeEach
  void setUp() {
    // 创建服务实例并手动设置mapper
    projectActivityService = new ProjectActivityServiceImpl();
    // 由于ServiceImpl需要BaseMapper，我们需要通过反射设置
    try {
      java.lang.reflect.Field field = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
      field.setAccessible(true);
      field.set(projectActivityService, projectActivityMapper);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set baseMapper", e);
    }
  }

  @Test
  @DisplayName("记录项目活动 - 成功")
  void shouldRecordActivity_Successfully() {
    // given
    Long projectId = 1L;
    Long userId = 1L;
    String activityType = "MEMBER_ADDED";
    String details = "User 'testuser' was added to the project.";
    
    when(projectActivityMapper.insert(any(ProjectActivity.class))).thenReturn(1);

    // when
    projectActivityService.recordActivity(projectId, userId, activityType, details);

    // then
    verify(projectActivityMapper).insert(any(ProjectActivity.class));
  }

  @Test
  @DisplayName("获取项目活动 - 成功")
  void shouldGetProjectActivities_Successfully() {
    // given
    Long projectId = 1L;
    Page<ProjectActivity> page = new Page<>(1, 10);
    page.setRecords(Collections.singletonList(new ProjectActivity()));
    page.setTotal(1);

    when(projectActivityMapper.selectPage(any(Page.class), any())).thenReturn(page);

    // when
    PageResult<ProjectActivity> result =
        projectActivityService.getProjectActivities(projectId, 1, 10);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getList()).hasSize(1);
    assertThat(result.getTotal()).isEqualTo(1);
    verify(projectActivityMapper).selectPage(any(), any());
  }
}
