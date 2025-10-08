<template>
  <div class="project-settings-tab">
    <a-card title="基本信息" class="settings-card">
      <a-form
        ref="basicFormRef"
        :model="basicForm"
        :rules="basicRules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="项目名称" name="name">
          <a-input
            v-model:value="basicForm.name"
            placeholder="请输入项目名称"
            :maxlength="100"
            show-count
          />
        </a-form-item>

        <a-form-item label="项目编码" name="code">
          <a-input
            v-model:value="basicForm.code"
            placeholder="项目编码"
            disabled
          />
          <template #extra>
            项目编码创建后不可修改
          </template>
        </a-form-item>

        <a-form-item label="项目描述" name="description">
          <a-textarea
            v-model:value="basicForm.description"
            placeholder="请输入项目描述"
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <a-form-item label="项目类型" name="type">
          <a-select v-model:value="basicForm.type" placeholder="请选择项目类型">
            <a-select-option value="WEB">Web应用</a-select-option>
            <a-select-option value="MOBILE">移动应用</a-select-option>
            <a-select-option value="DESKTOP">桌面应用</a-select-option>
            <a-select-option value="API">API服务</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="优先级" name="priority">
          <a-radio-group v-model:value="basicForm.priority">
            <a-radio :value="1">低</a-radio>
            <a-radio :value="2">中</a-radio>
            <a-radio :value="3">高</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="项目颜色" name="color">
          <a-input
            v-model:value="basicForm.color"
            type="color"
            style="width: 100px"
          />
        </a-form-item>

        <a-form-item label="起止时间" name="dateRange">
          <a-range-picker
            v-model:value="dateRange"
            format="YYYY-MM-DD"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
          <a-space>
            <a-button type="primary" :loading="saving" @click="handleSaveBasicInfo">
              保存更改
            </a-button>
            <a-button @click="handleResetBasicInfo">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="可见性设置" class="settings-card">
      <a-form
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="项目可见性">
          <a-radio-group v-model:value="visibilityForm.isPublic">
            <a-radio :value="true">
              <div>
                <div><strong>公开</strong></div>
                <div class="radio-description">所有组织成员都可以查看此项目</div>
              </div>
            </a-radio>
            <a-radio :value="false">
              <div>
                <div><strong>私有</strong></div>
                <div class="radio-description">只有项目成员可以查看此项目</div>
              </div>
            </a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="允许加入">
          <a-switch
            v-model:checked="visibilityForm.allowJoin"
            checked-children="开启"
            un-checked-children="关闭"
          />
          <template #extra>
            开启后,组织成员可以自行申请加入项目
          </template>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
          <a-button type="primary" :loading="saving" @click="handleSaveVisibility">
            保存更改
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="危险操作" class="settings-card danger-zone">
      <a-space direction="vertical" style="width: 100%">
        <div class="danger-item">
          <div class="danger-item-content">
            <h4>归档项目</h4>
            <p>归档后项目将变为只读状态,可以随时恢复</p>
          </div>
          <a-button
            v-if="project?.status !== 3"
            danger
            @click="handleArchive"
          >
            归档项目
          </a-button>
          <a-button
            v-else
            type="primary"
            @click="handleRestore"
          >
            恢复项目
          </a-button>
        </div>

        <a-divider />

        <div class="danger-item">
          <div class="danger-item-content">
            <h4>删除项目</h4>
            <p class="text-danger">
              <ExclamationCircleOutlined />
              删除后无法恢复,请谨慎操作!
            </p>
          </div>
          <a-button danger @click="handleDelete">
            删除项目
          </a-button>
        </div>
      </a-space>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import dayjs from 'dayjs'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
import { useProjectStore } from '@/stores/modules/project'
import type { Project, UpdateProjectRequest } from '@/types/project'

interface Props {
  project: Project | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  refresh: []
}>()

const router = useRouter()
const projectStore = useProjectStore()

// 响应式数据
const basicFormRef = ref<FormInstance>()
const saving = ref(false)
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

const basicForm = ref({
  name: '',
  code: '',
  description: '',
  type: undefined as string | undefined,
  priority: 1,
  color: '#1890ff',
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined
})

const visibilityForm = ref({
  isPublic: true,
  allowJoin: false
})

// 表单验证规则
const basicRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度在2-100个字符之间', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '项目编码不能为空' }
  ]
}

// 方法
const initForm = () => {
  if (props.project) {
    basicForm.value = {
      name: props.project.name,
      code: props.project.code,
      description: props.project.description || '',
      type: props.project.type,
      priority: props.project.priority || 1,
      color: props.project.color || '#1890ff',
      startDate: props.project.startDate,
      endDate: props.project.endDate
    }

    // 设置日期范围
    if (props.project.startDate && props.project.endDate) {
      dateRange.value = [
        dayjs(props.project.startDate),
        dayjs(props.project.endDate)
      ]
    }

    // 设置可见性
    visibilityForm.value = {
      isPublic: props.project.isPublic ?? true,
      allowJoin: props.project.allowJoin ?? false
    }
  }
}

const handleSaveBasicInfo = async () => {
  try {
    await basicFormRef.value?.validate()
    saving.value = true

    // 处理日期范围
    if (dateRange.value) {
      basicForm.value.startDate = dateRange.value[0].format('YYYY-MM-DD')
      basicForm.value.endDate = dateRange.value[1].format('YYYY-MM-DD')
    } else {
      basicForm.value.startDate = undefined
      basicForm.value.endDate = undefined
    }

    const updateData: UpdateProjectRequest = {
      name: basicForm.value.name,
      description: basicForm.value.description,
      type: basicForm.value.type,
      priority: basicForm.value.priority,
      color: basicForm.value.color,
      startDate: basicForm.value.startDate,
      endDate: basicForm.value.endDate
    }

    await projectStore.updateProjectInfo(props.project!.id, updateData)
    message.success('项目信息更新成功')
    emit('refresh')
  } catch (error) {
    console.error('Save basic info failed:', error)
  } finally {
    saving.value = false
  }
}

const handleResetBasicInfo = () => {
  initForm()
  message.info('已重置为原始值')
}

const handleSaveVisibility = async () => {
  try {
    saving.value = true

    const updateData: UpdateProjectRequest = {
      isPublic: visibilityForm.value.isPublic,
      allowJoin: visibilityForm.value.allowJoin
    }

    await projectStore.updateProjectInfo(props.project!.id, updateData)
    message.success('可见性设置更新成功')
    emit('refresh')
  } catch (error) {
    console.error('Save visibility failed:', error)
  } finally {
    saving.value = false
  }
}

const handleArchive = () => {
  Modal.confirm({
    title: '确认归档',
    content: '确定要归档这个项目吗?归档后项目将变为只读状态,可以随时恢复。',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await projectStore.archiveProjectById(props.project!.id)
        message.success('项目已归档')
        emit('refresh')
      } catch (error) {
        console.error('Archive project failed:', error)
      }
    }
  })
}

const handleRestore = async () => {
  try {
    await projectStore.restoreProjectById(props.project!.id)
    message.success('项目已恢复')
    emit('refresh')
  } catch (error) {
    console.error('Restore project failed:', error)
  }
}

const handleDelete = () => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这个项目吗?此操作不可恢复!',
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await projectStore.deleteProjectById(props.project!.id)
        message.success('项目删除成功')
        router.push('/projects')
      } catch (error) {
        console.error('Delete project failed:', error)
      }
    }
  })
}

// 监听项目变化
watch(() => props.project, () => {
  initForm()
}, { immediate: true })

// 生命周期
onMounted(() => {
  initForm()
})
</script>

<style lang="scss" scoped>
.project-settings-tab {
  .settings-card {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .radio-description {
    font-size: 12px;
    color: #8c8c8c;
    margin-top: 4px;
  }

  .danger-zone {
    border-color: #ff4d4f;

    :deep(.ant-card-head) {
      background-color: #fff2f0;
      border-bottom-color: #ffccc7;
    }

    .danger-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 0;

      &-content {
        flex: 1;

        h4 {
          margin: 0 0 8px;
          font-size: 16px;
          font-weight: 600;
        }

        p {
          margin: 0;
          color: #8c8c8c;
          font-size: 14px;

          &.text-danger {
            color: #ff4d4f;
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }
    }
  }
}
</style>

