
import { mount, flushPromises } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ProjectFormModal from '../ProjectFormModal.vue';
import { projectApi } from '@/api/modules/project';
import { message } from 'ant-design-vue';

// Mock the API module
vi.mock('@/api/modules/project', () => ({
  projectApi: {
    createProject: vi.fn(),
    updateProject: vi.fn(),
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

describe('ProjectFormModal.vue', () => {
  const mockUsers = [
    { id: 1, name: 'Admin User' },
    { id: 2, name: 'Test User' },
  ];

  const mockProject = {
    id: 101,
    name: 'Existing Project',
    key: 'EXIST',
    description: 'An existing project desc',
    leaderId: 1,
    startDate: '2025-10-01',
    endDate: '2025-10-31',
    status: 'ACTIVE',
    visibility: 'PUBLIC',
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders in create mode correctly', () => {
    const wrapper = mount(ProjectFormModal, {
      props: {
        visible: true,
        users: mockUsers,
      },
    });
    expect(wrapper.text()).toContain('创建项目');
    const keyInput = wrapper.find('input[placeholder="例如: PROJ (2-10个大写字母)"]');
    expect((keyInput.element as HTMLInputElement).disabled).toBe(false);
  });

  it('renders in edit mode correctly and populates form', async () => {
    const wrapper = mount(ProjectFormModal, {
      props: {
        visible: true,
        users: mockUsers,
        project: mockProject,
      },
    });
    await flushPromises();

    expect(wrapper.text()).toContain('编辑项目');
    const nameInput = wrapper.find('input[placeholder="请输入项目名称 (2-100个字符)"]');
    expect((nameInput.element as HTMLInputElement).value).toBe(mockProject.name);
    
    const keyInput = wrapper.find('input[placeholder="例如: PROJ (2-10个大写字母)"]');
    expect((keyInput.element as HTMLInputElement).disabled).toBe(true);
  });

  it('triggers validation error for required fields', async () => {
    const wrapper = mount(ProjectFormModal, {
      props: { visible: true, users: mockUsers },
    });

    const form = wrapper.findComponent({ name: 'AForm' });
    await form.trigger('submit');
    await flushPromises();

    expect(wrapper.text()).toContain('请输入项目名称');
    expect(wrapper.text()).toContain('请输入项目标识符');
    expect(projectApi.createProject).not.toHaveBeenCalled();
  });

  it('calls createProject on submit in create mode', async () => {
    vi.mocked(projectApi.createProject).mockResolvedValue({});
    const wrapper = mount(ProjectFormModal, {
      props: { visible: true, users: mockUsers },
    });

    // Fill form
    await wrapper.find('input[name="name"]').setValue('New Project');
    await wrapper.find('input[name="key"]').setValue('NEW');
    // ASelect is complex to interact with, so we set the model value directly
    await wrapper.vm.formState.leaderId = 2;
    
    // Submit
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(projectApi.createProject).toHaveBeenCalledWith(expect.objectContaining({
      name: 'New Project',
      key: 'NEW',
      leaderId: 2,
    }));
    expect(message.success).toHaveBeenCalledWith('项目创建成功');
    expect(wrapper.emitted('success')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });

  it('calls updateProject on submit in edit mode', async () => {
    vi.mocked(projectApi.updateProject).mockResolvedValue({});
    const wrapper = mount(ProjectFormModal, {
      props: { visible: true, users: mockUsers, project: mockProject },
    });
    await flushPromises();

    // Change a value
    const newName = 'Updated Project Name';
    await wrapper.find('input[name="name"]').setValue(newName);

    // Submit
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(projectApi.updateProject).toHaveBeenCalledWith(mockProject.id, expect.objectContaining({
      name: newName,
    }));
    expect(message.success).toHaveBeenCalledWith('项目更新成功');
    expect(wrapper.emitted('success')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });

  it('emits update:visible on cancel', async () => {
    const wrapper = mount(ProjectFormModal, {
      props: { visible: true },
    });

    // Find the cancel button in the modal footer and click it
    const cancelButton = wrapper.findAll('.ant-modal-footer button').find(btn => btn.text() === 'Cancel');
    if (cancelButton) {
        await cancelButton.trigger('click');
    } else {
        // Fallback for different Antd versions or custom footers
        await wrapper.vm.handleCancel();
    }

    expect(wrapper.emitted('update:visible')).toBeTruthy();
    expect(wrapper.emitted('update:visible')![0]).toEqual([false]);
  });
});
