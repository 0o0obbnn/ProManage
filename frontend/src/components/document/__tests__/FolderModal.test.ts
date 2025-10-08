import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import FolderModal from '../FolderModal.vue'

// Mock API
vi.mock('@/api/modules/document', () => ({
  createFolder: vi.fn(() => Promise.resolve({ id: 1, name: 'Test Folder' })),
  updateFolder: vi.fn(() => Promise.resolve({ id: 1, name: 'Updated Folder' }))
}))

describe('FolderModal', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render correctly when open', () => {
    const wrapper = mount(FolderModal, {
      props: {
        open: true,
        folders: [],
        projects: []
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-tree-select': true,
          'a-select': true,
          'a-select-option': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should show "新建文件夹" title when folder is null', () => {
    const wrapper = mount(FolderModal, {
      props: {
        open: true,
        folder: null,
        folders: [],
        projects: []
      },
      global: {
        stubs: {
          'a-modal': {
            template: '<div><slot /></div>',
            props: ['title']
          },
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-tree-select': true,
          'a-select': true
        }
      }
    })

    const vm = wrapper.vm as any
    expect(vm.isEdit).toBe(false)
  })

  it('should show "编辑文件夹" title when folder is provided', () => {
    const folder = {
      id: 1,
      name: 'Test Folder',
      parentId: undefined,
      projectId: 1,
      path: '/Test Folder',
      level: 1,
      documentCount: 0,
      createdAt: '2024-01-01',
      updatedAt: '2024-01-01'
    }

    const wrapper = mount(FolderModal, {
      props: {
        open: true,
        folder,
        folders: [],
        projects: []
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-tree-select': true,
          'a-select': true
        }
      }
    })

    const vm = wrapper.vm as any
    expect(vm.isEdit).toBe(true)
  })

  it('should validate folder name', () => {
    const wrapper = mount(FolderModal, {
      props: {
        open: true,
        folders: [],
        projects: []
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-tree-select': true,
          'a-select': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Test required validation
    const requiredRule = vm.rules.name[0]
    expect(requiredRule.required).toBe(true)
    expect(requiredRule.message).toBe('请输入文件夹名称')

    // Test length validation
    const lengthRule = vm.rules.name[1]
    expect(lengthRule.min).toBe(1)
    expect(lengthRule.max).toBe(50)
  })

  it('should validate special characters in folder name', async () => {
    const wrapper = mount(FolderModal, {
      props: {
        open: true,
        folders: [],
        projects: []
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-tree-select': true,
          'a-select': true
        }
      }
    })

    const vm = wrapper.vm as any
    const specialCharRule = vm.rules.name[2]
    
    // Test invalid characters
    const invalidNames = ['test<folder', 'test>folder', 'test:folder', 'test"folder', 
                          'test/folder', 'test\\folder', 'test|folder', 'test?folder', 'test*folder']
    
    for (const name of invalidNames) {
      try {
        await specialCharRule.validator({}, name)
        expect(true).toBe(false) // Should not reach here
      } catch (error: any) {
        expect(error).toContain('不能包含特殊字符')
      }
    }

    // Test valid name
    const validName = 'test_folder-123'
    const result = await specialCharRule.validator({}, validName)
    expect(result).toBeUndefined()
  })

  it('should call createFolder API when creating new folder', async () => {
    const { createFolder } = await import('@/api/modules/document')
    
    const wrapper = mount(FolderModal, {
      props: {
        open: true,
        folders: [],
        projects: [{ id: 1, name: 'Project 1' }]
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': {
            template: '<div><slot /></div>'
          },
          'a-form-item': true,
          'a-input': true,
          'a-tree-select': true,
          'a-select': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.formData.name = 'New Folder'
    vm.formData.projectId = 1

    // Mock form validation
    vm.formRef = {
      validate: vi.fn(() => Promise.resolve())
    }

    await vm.handleOk()

    expect(createFolder).toHaveBeenCalledWith({
      name: 'New Folder',
      parentId: undefined,
      projectId: 1
    })
  })

  it('should call updateFolder API when editing folder', async () => {
    const { updateFolder } = await import('@/api/modules/document')
    
    const folder = {
      id: 1,
      name: 'Old Folder',
      parentId: undefined,
      projectId: 1,
      path: '/Old Folder',
      level: 1,
      documentCount: 0,
      createdAt: '2024-01-01',
      updatedAt: '2024-01-01'
    }

    const wrapper = mount(FolderModal, {
      props: {
        open: true,
        folder,
        folders: [],
        projects: []
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': {
            template: '<div><slot /></div>'
          },
          'a-form-item': true,
          'a-input': true,
          'a-tree-select': true,
          'a-select': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.formData.name = 'Updated Folder'

    // Mock form validation
    vm.formRef = {
      validate: vi.fn(() => Promise.resolve())
    }

    await vm.handleOk()

    expect(updateFolder).toHaveBeenCalledWith(1, {
      name: 'Updated Folder'
    })
  })

  it('should exclude current folder and its children from parent selection when editing', () => {
    const folders = [
      {
        id: 1,
        name: 'Folder 1',
        children: [
          { id: 2, name: 'Folder 1.1', children: [] },
          { id: 3, name: 'Folder 1.2', children: [] }
        ]
      },
      {
        id: 4,
        name: 'Folder 2',
        children: []
      }
    ]

    const currentFolder = folders[0]

    const wrapper = mount(FolderModal, {
      props: {
        open: true,
        folder: currentFolder as any,
        folders: folders as any,
        projects: []
      },
      global: {
        stubs: {
          'a-modal': true,
          'a-form': true,
          'a-form-item': true,
          'a-input': true,
          'a-tree-select': true,
          'a-select': true
        }
      }
    })

    const vm = wrapper.vm as any
    const treeData = vm.folderTreeData

    // Should only contain Folder 2
    expect(treeData.length).toBe(1)
    expect(treeData[0].id).toBe(4)
  })
})

