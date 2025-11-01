package com.promanage.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mockito.MockedStatic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promanage.common.entity.User;
import com.promanage.common.exception.BusinessException;
import com.promanage.infrastructure.security.SecurityUtils;
import com.promanage.domain.mapper.UserMapper;

/** UserServiceImpl 单元测试 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 单元测试")
class UserServiceImplTest {

  @Mock private UserMapper userMapper;

  @InjectMocks private UserServiceImpl userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("encodedPassword");
    testUser.setEmail("test@example.com");
    testUser.setDeleted(false);
  }

  @Disabled("Test is broken due to missing mockito-inline dependency")
  @Test
  @DisplayName("should get user by id successfully")
  void shouldGetUserById_whenUserExists() {
    try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
      mocked.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(1L));
      // given
      Long userId = 1L;
      when(userMapper.selectById(userId)).thenReturn(testUser);

      // when
      User result = userService.getById(userId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(userId);
      verify(userMapper).selectById(userId);
    }
  }

  @Disabled("Test is broken due to missing mockito-inline dependency")
  @Test
  @DisplayName("should throw exception when user not found")
  void shouldThrowException_whenUserNotFound() {
    try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
      mocked.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of(1L));
      // given
      Long userId = 999L;
      when(userMapper.selectById(userId)).thenReturn(null);

      // when & then
      assertThatThrownBy(() -> userService.getById(userId)).isInstanceOf(BusinessException.class);

      verify(userMapper).selectById(userId);
    }
  }

  @Test
  @DisplayName("should get users by ids successfully")
  void shouldGetUsersByIds_whenAllUsersExist() {
    // given
    User user1 = new User();
    user1.setId(1L);
    user1.setUsername("user1");
    user1.setDeleted(false);

    User user2 = new User();
    user2.setId(2L);
    user2.setUsername("user2");
    user2.setDeleted(false);

    List<Long> ids = Arrays.asList(1L, 2L);
    List<User> users = Arrays.asList(user1, user2);

    when(userMapper.selectBatchIds(ids)).thenReturn(users);

    // when
    Map<Long, User> result = userService.getByIds(ids);

    // then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
    assertThat(result.get(1L).getUsername()).isEqualTo("user1");
    assertThat(result.get(2L).getUsername()).isEqualTo("user2");
    verify(userMapper).selectBatchIds(ids);
  }

  @Test
  @DisplayName("should filter out deleted users")
  void shouldFilterDeletedUsers_whenGettingByIds() {
    // given
    User user1 = new User();
    user1.setId(1L);
    user1.setDeleted(false);

    User user2 = new User();
    user2.setId(2L);
    user2.setDeleted(true); // Deleted

    List<Long> ids = Arrays.asList(1L, 2L);
    List<User> users = Arrays.asList(user1, user2);

    when(userMapper.selectBatchIds(ids)).thenReturn(users);

    // when
    Map<Long, User> result = userService.getByIds(ids);

    // then
    assertThat(result).hasSize(1);
    assertThat(result).containsKey(1L);
    assertThat(result).doesNotContainKey(2L);
  }
}
