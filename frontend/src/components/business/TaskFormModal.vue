
<template>
  <a-modal
    :open="visible"
    :title="isEditMode ? '编辑任务' : '创建任务'"
    :confirm-loading="loading"
    width="720px"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      layout="vertical"
      name="taskForm"
    >
      <a-form-item label="任务标题" name="title">
        <a-input v-model:value="formState.title" placeholder="请输入任务标题" />
      </a-form-item>

      <a-form-item label="任务描述" name="description">
        <a-textarea v-model:value="formState.description" :rows="5" placeholder="请输入任务详细描述" />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="指派给" name="assigneeId">
            <a-select
              v-model:value="formState.assigneeId"
              show-search
              placeholder="请选择指派的成员"
              :filter-option="filterUserOption"
              allow-clear
            >
              <a-select-option v-for="user in users" :key="user.id" :value="user.id">
                {{ user.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="报告人" name="reporterId">
            <a-select
              v-model:value="formState.reporterId"
              show-search
              placeholder="请选择报告人"
              :filter-option="filterUserOption"
            >
              <a-select-option v-for="user in users" :key="user.id" :value="user.id">
                {{ user.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="任务状态" name="status">
            <a-select v-model:value="formState.status" placeholder="请选择任务状态">
              <a-select-option value="TODO">待办</a-select-option>
              <a-select-option value="IN_PROGRESS">进行中</a-select-option>
              <a-select-option value="IN_REVIEW">评审中</a-select-option>
              <a-select-option value="DONE">已完成</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="优先级" name="priority">
            <a-select v-model:value="formState.priority" placeholder="请设置优先级">
              <a-select-option value="LOW">低</a-select-option>
              <a-select-option value="MEDIUM">中</a-select-option>
              <a-select-option value="HIGH">高</a-select-option>
              <a-select-option value="URGENT">紧急</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="截止日期" name="dueDate">
            <a-date-picker v-model:value="formState.dueDate" style="width: 100%" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="预计工时(小时)" name="estimatedHours">
            <a-input-number v-model:value="formState.estimatedHours" :min="0" style="width: 100%" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="标签" name="labels">
        <a-select
          v-model:value="formState.labels"
          mode="tags"
          style="width: 100%"
          placeholder="输入并按回车添加标签"
        ></a-select>
      </a-form-item>

    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import type { PropType } from 'vue';
import { message } from 'ant-design-vue';
import type { FormInstance, Rule } from 'ant-design-vue/lib/form';
import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';
import { taskApi } from '@/api/modules/task'; // 假设API已创建
import { useUserStore } from '@/stores/modules/user';

// --- 类型定义 ---
interface User {
  id: number;
  name: string;
}

interface FormState {
  title: string;
  description: string;
  assigneeId: number | null;
  reporterId: number | null;
  status: string;
  priority: string;
  dueDate: Dayjs | null;
  estimatedHours: number | null;
  labels: string[];
}

interface TaskData {
  id?: number;
  title: string;
  description?: string;
  assigneeId?: number;
  reporterId?: number;
  status?: string;
  priority?: string;
  dueDate?: string;
  estimatedHours?: number;
  labels?: string[];
}

// --- Props --- 
const props = defineProps({
  visible: {
    type: Boolean,
    required: true,
  },
  task: {
    type: Object as PropType<TaskData | null>,
    default: null,
  },
  projectId: {
    type: Number,
    required: true,
  },
  users: {
    type: Array as PropType<User[]>,
    default: () => [],
  },
});

// --- Emits ---
const emit = defineEmits(['update:visible', 'success']);

// --- 响应式状态 ---
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const getDefaultFormState = (): FormState => ({
  title: '',
  description: '',
  assigneeId: null,
  reporterId: userStore.userInfo?.id || null,
  status: 'TODO',
  priority: 'MEDIUM',
  dueDate: null,
  estimatedHours: null,
  labels: [],
});

const formState = ref<FormState>(getDefaultFormState());

// --- 计算属性 ---
const isEditMode = computed(() => !!props.task);

// --- 表单验证规则 ---
const rules: Record<string, Rule[]> = {
  title: [
    { required: true, message: '请输入任务标题' },
    { max: 200, message: '标题长度不超过 200 个字符', trigger: 'blur' },
  ],
  reporterId: [
    { required: true, message: '请选择报告人' },
  ],
};

// --- 监听器 ---
watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    if (props.task) {
      // 编辑模式
      const { task } = props;
      formState.value = {
        title: task.title,
        description: task.description || '',
        assigneeId: task.assigneeId || null,
        reporterId: task.reporterId || userStore.userInfo?.id || null,
        status: task.status || 'TODO',
        priority: task.priority || 'MEDIUM',
        dueDate: task.dueDate ? dayjs(task.dueDate) : null,
        estimatedHours: task.estimatedHours || null,
        labels: task.labels || [],
      };
    } else {
      // 创建模式
      formRef.value?.resetFields();
      formState.value = getDefaultFormState();
    }
  }
});

// --- 方法 ---
const handleCancel = () => {
  emit('update:visible', false);
};

const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    loading.value = true;

    const submissionData = {
      ...formState.value,
      projectId: props.projectId,
      dueDate: formState.value.dueDate?.format('YYYY-MM-DD'),
    };

    if (isEditMode.value && props.task?.id) {
      // 编辑模式
      await taskApi.updateTask(props.task.id, submissionData);
      message.success('任务更新成功');
    } else {
      // 创建模式
      await taskApi.createTask(submissionData);
      message.success('任务创建成功');
    }

    emit('success');
    emit('update:visible', false);
  } catch (error) {
    console.error('任务表单提交失败:', error);
  } finally {
    loading.value = false;
  }
};

const filterUserOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase());
};

</script>
