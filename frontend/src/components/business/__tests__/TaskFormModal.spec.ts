
import { mount, flushPromises } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import TaskFormModal from '../TaskFormModal.vue';
import { taskApi } from '@/api/modules/task';
import { useUserStore } from '@/stores/modules/user';
import { message } from 'ant-design-vue';

// Mock the API module
vi.mock('@/api/modules/task', () => ({
  taskApi: {
    createTask: vi.fn(),
    updateTask: vi.fn(),
  },
}));

// Mock Ant Design message
vi.mock('ant-design-vue', async () => {
  const actual = await vi.importActual('ant-design-vue');
  return {
    ...actual,
    message: {
      success: vi.fn(),
      error: vi.fn(),
    },
  };
});

describe('TaskFormModal.vue', () => {
  const mockUsers = [
    { id: 1, name: 'Admin User' },
    { id: 2, name: 'Test User' },
  ];

  const mockTask = {
    id: 101,
    title: 'Existing Task',
    description: 'An existing task desc',
    assigneeId: 1,
    reporterId: 2,
    status: 'IN_PROGRESS',
    priority: 'HIGH',
    dueDate: '2025-10-20',
  };

  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    // Mock user store to provide a default reporter
    const userStore = useUserStore();
    userStore.userInfo = { id: 2, name: 'Test User' } as any;
  });

  it('renders in create mode and sets default reporter', async () => {
    const wrapper = mount(TaskFormModal, {
      props: {
        visible: true,
        users: mockUsers,
        projectId: 1,
      },
    });
    await flushPromises();

    expect(wrapper.text()).toContain('创建任务');
    // Check if reporterId is defaulted to current user
    expect(wrapper.vm.formState.reporterId).toBe(2);
  });

  it('renders in edit mode and populates form', async () => {
    const wrapper = mount(TaskFormModal, {
      props: {
        visible: true,
        users: mockUsers,
        projectId: 1,
        task: mockTask,
      },
    });
    await flushPromises();

    expect(wrapper.text()).toContain('编辑任务');
    const titleInput = wrapper.find('input[placeholder="请输入任务标题"]');
    expect((titleInput.element as HTMLInputElement).value).toBe(mockTask.title);
    expect(wrapper.vm.formState.priority).toBe('HIGH');
  });

  it('triggers validation error for title', async () => {
    const wrapper = mount(TaskFormModal, {
      props: { visible: true, users: mockUsers, projectId: 1 },
    });

    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.text()).toContain('请输入任务标题');
    expect(taskApi.createTask).not.toHaveBeenCalled();
  });

  it('calls createTask on submit in create mode', async () => {
    vi.mocked(taskApi.createTask).mockResolvedValue({});
    const wrapper = mount(TaskFormModal, {
      props: { visible: true, users: mockUsers, projectId: 99 },
    });

    // Fill form
    await wrapper.find('input[name="title"]').setValue('New Awesome Task');
    await wrapper.vm.formState.assigneeId = 1;
    
    // Submit
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(taskApi.createTask).toHaveBeenCalledWith(expect.objectContaining({
      title: 'New Awesome Task',
      assigneeId: 1,
      projectId: 99, // Ensure projectId is passed
    }));
    expect(message.success).toHaveBeenCalledWith('任务创建成功');
    expect(wrapper.emitted('success')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });

  it('calls updateTask on submit in edit mode', async () => {
    vi.mocked(taskApi.updateTask).mockResolvedValue({});
    const wrapper = mount(TaskFormModal, {
      props: { visible: true, users: mockUsers, projectId: 1, task: mockTask },
    });
    await flushPromises();

    // Change a value
    const newTitle = 'Updated Task Title';
    await wrapper.find('input[name="title"]').setValue(newTitle);

    // Submit
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(taskApi.updateTask).toHaveBeenCalledWith(mockTask.id, expect.objectContaining({
      title: newTitle,
    }));
    expect(message.success).toHaveBeenCalledWith('任务更新成功');
    expect(wrapper.emitted('success')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });
});
