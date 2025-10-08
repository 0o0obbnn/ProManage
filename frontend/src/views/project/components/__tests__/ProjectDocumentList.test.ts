import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProjectDocumentList from '../ProjectDocumentList.vue'
import { useDocumentStore } from '@/stores/modules/document'

// Define FileType enum locally for testing
enum FileType {
  PDF = 'PDF',
  WORD = 'WORD',
  EXCEL = 'EXCEL',
  PPT = 'PPT',
  IMAGE = 'IMAGE',
  VIDEO = 'VIDEO',
  AUDIO = 'AUDIO',
  ZIP = 'ZIP',
  OTHER = 'OTHER'
}

// Mock Ant Design Vue components
vi.mock('ant-design-vue', () => ({
  message: {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn()
  },
  Modal: {
    confirm: vi.fn()
  }
}))

// Mock router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

describe('ProjectDocumentList', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should render correctly', () => {
    const wrapper = mount(ProjectDocumentList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'a-row': true,
          'a-col': true,
          'a-card': true,
          'a-card-meta': true,
          'a-spin': true,
          'a-empty': true,
          'CreateDocumentModal': true,
          'UploadModal': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('should have correct view modes', () => {
    const wrapper = mount(ProjectDocumentList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'a-row': true,
          'a-col': true,
          'a-card': true,
          'a-card-meta': true,
          'a-spin': true,
          'a-empty': true,
          'CreateDocumentModal': true,
          'UploadModal': true
        }
      }
    })

    const vm = wrapper.vm as any
    expect(['list', 'grid']).toContain(vm.currentViewMode)
  })

  it('should filter documents by search keyword', async () => {
    const wrapper = mount(ProjectDocumentList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'a-row': true,
          'a-col': true,
          'a-card': true,
          'a-card-meta': true,
          'a-spin': true,
          'a-empty': true,
          'CreateDocumentModal': true,
          'UploadModal': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Set mock documents
    vm.documents = [
      { id: 1, name: 'Document 1', fileType: FileType.PDF, size: 1024 },
      { id: 2, name: 'Document 2', fileType: FileType.WORD, size: 2048 },
      { id: 3, name: 'Another Doc', fileType: FileType.EXCEL, size: 3072 }
    ]

    // Test search
    vm.searchKeyword = 'Document 1'
    await wrapper.vm.$nextTick()

    expect(vm.filteredDocuments.length).toBe(1)
    expect(vm.filteredDocuments[0].name).toBe('Document 1')
  })

  it('should filter documents by file type', async () => {
    const wrapper = mount(ProjectDocumentList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'a-row': true,
          'a-col': true,
          'a-card': true,
          'a-card-meta': true,
          'a-spin': true,
          'a-empty': true,
          'CreateDocumentModal': true,
          'UploadModal': true
        }
      }
    })

    const vm = wrapper.vm as any
    
    // Set mock documents
    vm.documents = [
      { id: 1, name: 'Document 1', fileType: FileType.PDF, size: 1024 },
      { id: 2, name: 'Document 2', fileType: FileType.WORD, size: 2048 },
      { id: 3, name: 'Document 3', fileType: FileType.PDF, size: 3072 }
    ]

    // Test file type filter
    vm.filterType = FileType.PDF
    await wrapper.vm.$nextTick()

    expect(vm.filteredDocuments.length).toBe(2)
    expect(vm.filteredDocuments.every((d: any) => d.fileType === FileType.PDF)).toBe(true)
  })

  it('should format file size correctly', () => {
    const wrapper = mount(ProjectDocumentList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'a-row': true,
          'a-col': true,
          'a-card': true,
          'a-card-meta': true,
          'a-spin': true,
          'a-empty': true,
          'CreateDocumentModal': true,
          'UploadModal': true
        }
      }
    })

    const vm = wrapper.vm as any

    expect(vm.formatFileSize(0)).toBe('-')
    expect(vm.formatFileSize(512)).toBe('512 B')
    expect(vm.formatFileSize(1024)).toBe('1.00 KB')
    expect(vm.formatFileSize(1024 * 1024)).toBe('1.00 MB')
  })

  it('should get correct type color', () => {
    const wrapper = mount(ProjectDocumentList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'a-row': true,
          'a-col': true,
          'a-card': true,
          'a-card-meta': true,
          'a-spin': true,
          'a-empty': true,
          'CreateDocumentModal': true,
          'UploadModal': true
        }
      }
    })

    const vm = wrapper.vm as any

    expect(vm.getTypeColor(FileType.PDF)).toBe('red')
    expect(vm.getTypeColor(FileType.WORD)).toBe('blue')
    expect(vm.getTypeColor(FileType.EXCEL)).toBe('green')
    expect(vm.getTypeColor(FileType.PPT)).toBe('orange')
  })

  it('should get correct type text', () => {
    const wrapper = mount(ProjectDocumentList, {
      props: {
        projectId: 1
      },
      global: {
        stubs: {
          'a-button': true,
          'a-radio-group': true,
          'a-radio-button': true,
          'a-input-search': true,
          'a-select': true,
          'a-select-option': true,
          'a-table': true,
          'a-row': true,
          'a-col': true,
          'a-card': true,
          'a-card-meta': true,
          'a-spin': true,
          'a-empty': true,
          'CreateDocumentModal': true,
          'UploadModal': true
        }
      }
    })

    const vm = wrapper.vm as any

    expect(vm.getTypeText(FileType.PDF)).toBe('PDF')
    expect(vm.getTypeText(FileType.WORD)).toBe('Word')
    expect(vm.getTypeText(FileType.EXCEL)).toBe('Excel')
    expect(vm.getTypeText(FileType.IMAGE)).toBe('图片')
  })
})

