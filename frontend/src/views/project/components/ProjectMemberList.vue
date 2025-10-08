<template>
  <a-card class="project-member-list" title="项目成员">
    <template #extra>
      <a-button type="link" size="small" @click="handleAddMember">
        <PlusOutlined />
        添加成员
      </a-button>
    </template>

    <!-- 加载状态 -->
    <div v-if="loading" class="project-member-list__loading">
      <a-spin />
    </div>

    <!-- 成员列表 -->
    <a-list
      v-else-if="members.length > 0"
      :data-source="members"
      :loading="loading"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta>
            <template #avatar>
              <a-avatar :size="40">
                {{ item.realName?.charAt(0) || item.username?.charAt(0) || 'U' }}
              </a-avatar>
            </template>
            <template #title>
              <div class="project-member-list__member-name">
                {{ item.realName || item.username }}
                <a-tag
                  v-if="item.roleId"
                  :color="getRoleColor(item.roleId)"
                  size="small"
                >
                  {{ item.roleName || getRoleText(item.roleId) }}
                </a-tag>
              </div>
            </template>
            <template #description>
              <div class="project-member-list__member-info">
                <span v-if="item.email">{{ item.email }}</span>
                <span v-if="item.joinedAt">加入时间: {{ formatDate(item.joinedAt) }}</span>
              </div>
            </template>
          </a-list-item-meta>

          <template #actions>
            <a-dropdown>
              <a-button type="text" size="small">
                <MoreOutlined />
              </a-button>
              <template #overlay>
                <a-menu @click="({ key }) => handleMenuClick(key, item)">
                  <a-menu-item key="edit">
                    <EditOutlined />
                    编辑角色
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="remove" danger>
                    <DeleteOutlined />
                    移除成员
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
        </a-list-item>
      </template>
    </a-list>

    <!-- 空状态 -->
    <a-empty
      v-else
      description="暂无成员"
      :image="Empty.PRESENTED_IMAGE_SIMPLE"
    >
      <a-button type="primary" @click="handleAddMember">
        <PlusOutlined />
        添加成员
      </a-button>
    </a-empty>

    <!-- 添加成员弹窗 -->
    <AddMemberModal
      v-model:open="addModalVisible"
      :project-id="projectId"
      :existing-member-ids="existingMemberIds"
      @success="handleAddSuccess"
    />

    <!-- 编辑成员角色弹窗 -->
    <EditMemberRoleModal
      v-model:open="editModalVisible"
      :project-id="projectId"
      :member="selectedMember"
      @success="handleEditSuccess"
    />
  </a-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Modal, message, Empty } from 'ant-design-vue'
import {
  PlusOutlined,
  MoreOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import type { ProjectMember } from '@/types/project'
import { format } from 'date-fns'
import { removeProjectMember } from '@/api/modules/project'
import AddMemberModal from './AddMemberModal.vue'
import EditMemberRoleModal from './EditMemberRoleModal.vue'

/**
 * 组件属性
 */
interface Props {
  projectId: number
  members: ProjectMember[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

/**
 * 组件事件
 */
interface Emits {
  (e: 'refresh'): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const addModalVisible = ref(false)
const editModalVisible = ref(false)
const selectedMember = ref<ProjectMember | null>(null)

/**
 * 已存在的成员ID列表
 */
const existingMemberIds = computed(() => {
  return props.members.map(m => m.userId)
})

/**
 * 获取角色颜色
 */
const getRoleColor = (roleId: number): string => {
  const colorMap: Record<number, string> = {
    1: 'red', // 项目经理
    2: 'blue', // 开发人员
    3: 'green', // 测试人员
    4: 'orange', // 设计师
    5: 'purple', // 运维人员
    6: 'default' // 访客
  }
  return colorMap[roleId] || 'default'
}

/**
 * 获取角色文本
 */
const getRoleText = (roleId: number): string => {
  const textMap: Record<number, string> = {
    1: '项目经理',
    2: '开发人员',
    3: '测试人员',
    4: '设计师',
    5: '运维人员',
    6: '访客'
  }
  return textMap[roleId] || '未知'
}

/**
 * 格式化日期
 */
const formatDate = (date: string): string => {
  try {
    return format(new Date(date), 'yyyy-MM-dd')
  } catch {
    return date
  }
}

/**
 * 处理添加成员
 */
const handleAddMember = () => {
  addModalVisible.value = true
}

/**
 * 处理添加成功
 */
const handleAddSuccess = () => {
  emit('refresh')
}

/**
 * 处理编辑成功
 */
const handleEditSuccess = () => {
  emit('refresh')
}

/**
 * 处理移除成员
 */
const handleRemoveMember = async (member: ProjectMember) => {
  Modal.confirm({
    title: '确认移除成员',
    content: `确定要将 ${member.realName || member.username} 从项目中移除吗?`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await removeProjectMember(props.projectId, member.userId)
        message.success('移除成员成功')
        emit('refresh')
      } catch (error: any) {
        console.error('Remove member failed:', error)
        message.error(error.message || '移除成员失败')
      }
    }
  })
}

/**
 * 处理菜单点击
 */
const handleMenuClick = (key: string, member: ProjectMember) => {
  switch (key) {
    case 'edit':
      selectedMember.value = member
      editModalVisible.value = true
      break
    case 'remove':
      handleRemoveMember(member)
      break
  }
}
</script>

<style lang="scss" scoped>
.project-member-list {
  margin-bottom: 24px;

  &__loading {
    display: flex;
    justify-content: center;
    padding: 40px 0;
  }

  &__member-name {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__member-info {
    font-size: 12px;
    color: #8c8c8c;
  }
}
</style>

