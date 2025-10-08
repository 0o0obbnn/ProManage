
<template>
  <a-modal
    :open="visible"
    :title="isEditMode ? '编辑项目' : '创建项目'"
    :confirm-loading="loading"
    width="600px"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      layout="vertical"
      name="projectForm"
    >
      <a-form-item label="项目名称" name="name">
        <a-input v-model:value="formState.name" placeholder="请输入项目名称 (2-100个字符)" />
      </a-form-item>

      <a-form-item label="项目标识符" name="key">
        <a-input
          v-model:value="formState.key"
          placeholder="例如: PROJ (2-10个大写字母)"
          :disabled="isEditMode"
        />
      </a-form-item>

      <a-form-item label="项目描述" name="description">
        <a-textarea v-model:value="formState.description" :rows="4" placeholder="请输入项目描述 (最多500字符)" />
      </a-form-item>

      <a-form-item label="项目负责人" name="leaderId">
        <a-select
          v-model:value="formState.leaderId"
          show-search
          placeholder="请选择项目负责人"
          :filter-option="filterUserOption"
        >
          <!-- 假设 users 是一个传入的 prop 或从 store 获取 -->
          <a-select-option v-for="user in users" :key="user.id" :value="user.id">
            {{ user.name }}
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="起止时间" name="dateRange">
        <a-range-picker v-model:value="formState.dateRange" style="width: 100%" />
      </a-form-item>

      <a-form-item label="项目状态" name="status">
        <a-select v-model:value="formState.status" placeholder="请选择项目状态">
          <a-select-option value="PLANNING">规划中</a-select-option>
          <a-select-option value="ACTIVE">进行中</a-select-option>
          <a-select-option value="ON_HOLD">暂停</a-select-option>
          <a-select-option value="COMPLETED">已完成</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="项目可见性" name="visibility">
        <a-radio-group v-model:value="formState.visibility">
          <a-radio value="PUBLIC">公开 (所有成员可见)</a-radio>
          <a-radio value="PRIVATE">私有 (仅项目成员可见)</a-radio>
        </a-radio-group>
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
import { projectApi } from '@/api/modules/project'; // 假设API已创建

// --- 类型定义 ---
interface User {
  id: number;
  name: string;
}

interface FormState {
  name: string;
  key: string;
  description: string;
  leaderId: number | null;
  dateRange: [Dayjs, Dayjs] | null;
  status: string;
  visibility: string;
}

interface ProjectData {
  id?: number;
  name: string;
  key: string;
  description: string;
  leaderId: number;
  startDate?: string;
  endDate?: string;
  status: string;
  visibility: string;
}

// --- Props --- 
const props = defineProps({
  visible: {
    type: Boolean,
    required: true,
  },
  project: {
    type: Object as PropType<ProjectData | null>,
    default: null,
  },
  users: {
    type: Array as PropType<User[]>,
    default: () => [],
  },
});

// --- Emits ---
const emit = defineEmits(['update:visible', 'success']);

// --- 响应式状态 ---
const formRef = ref<FormInstance>();
const loading = ref(false);
const formState = ref<FormState>({
  name: '',
  key: '',
  description: '',
  leaderId: null,
  dateRange: null,
  status: 'PLANNING',
  visibility: 'PRIVATE',
});

// --- 计算属性 ---
const isEditMode = computed(() => !!props.project);

// --- 表单验证规则 ---
const rules: Record<string, Rule[]> = {
  name: [
    { required: true, message: '请输入项目名称' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' },
  ],
  key: [
    { required: true, message: '请输入项目标识符' },
    { pattern: /^[A-Z0-9_-]{2,50}$/, message: '需为2-50个大写字母、数字、下划线或连字符', trigger: 'blur' },
    // 唯一性校验应在提交时由后端处理
  ],
  description: [
      { max: 500, message: '长度不超过 500 个字符', trigger: 'blur' },
  ],
  leaderId: [
    { required: true, message: '请选择项目负责人' },
  ],
};

// --- 监听器 ---
watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    if (props.project) {
      // 编辑模式: 填充表单
      const { project } = props;
      formState.value = {
        name: project.name,
        key: project.key,
        description: project.description,
        leaderId: project.leaderId,
        dateRange: project.startDate && project.endDate ? [dayjs(project.startDate), dayjs(project.endDate)] : null,
        status: project.status,
        visibility: project.visibility,
      };
    } else {
      // 创建模式: 重置表单
      formRef.value?.resetFields();
      formState.value = {
        name: '',
        key: '',
        description: '',
        leaderId: null,
        dateRange: null,
        status: 'PLANNING',
        visibility: 'PRIVATE',
      };
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

    const [startDate, endDate] = formState.value.dateRange || [null, null];
    const submissionData = {
      ...formState.value,
      startDate: startDate?.format('YYYY-MM-DD'),
      endDate: endDate?.format('YYYY-MM-DD'),
      dateRange: undefined, // 从提交数据中移除
    };

    if (isEditMode.value && props.project?.id) {
      // 编辑模式
      await projectApi.updateProject(props.project.id, submissionData);
      message.success('项目更新成功');
    } else {
      // 创建模式
      await projectApi.createProject(submissionData);
      message.success('项目创建成功');
    }

    emit('success');
    emit('update:visible', false);
  } catch (error) {
    console.error('表单提交失败:', error);
    // 错误消息已在API层统一处理, 这里不再重复提示
  } finally {
    loading.value = false;
  }
};

const filterUserOption = (input: string, option: any) => {
  return option.children[0].children.toLowerCase().includes(input.toLowerCase());
};

</script>
