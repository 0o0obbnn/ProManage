package com.promanage.service.impl;

import com.promanage.service.entity.DocumentFolder;
import com.promanage.service.mapper.DocumentFolderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentFolderServiceImplTest {

    @Mock
    private DocumentFolderMapper documentFolderMapper;

    @InjectMocks
    private DocumentFolderServiceImpl documentFolderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createFolder_shouldCreateFolderSuccessfully() {
        // Given
        DocumentFolder folder = new DocumentFolder();
        folder.setName("Test Folder");
        folder.setProjectId(1L);
        folder.setCreatorId(1L);
        folder.setDeleted(false);

        when(documentFolderMapper.insert(any(DocumentFolder.class))).thenAnswer(invocation -> {
            DocumentFolder f = invocation.getArgument(0);
            f.setId(1L);
            return 1;
        });

        // When
        Long folderId = documentFolderService.createFolder(folder);

        // Then
        assertNotNull(folderId);
        assertEquals(1L, folderId);
        verify(documentFolderMapper, times(1)).insert(any(DocumentFolder.class));
    }

    @Test
    void getFolderById_shouldReturnFolderWhenExists() {
        // Given
        DocumentFolder folder = new DocumentFolder();
        folder.setId(1L);
        folder.setName("Test Folder");
        folder.setProjectId(1L);
        folder.setCreatorId(1L);
        folder.setDeleted(false);

        when(documentFolderMapper.selectById(1L)).thenReturn(folder);

        // When
        DocumentFolder result = documentFolderService.getFolderById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Test Folder", result.getName());
        verify(documentFolderMapper, times(1)).selectById(1L);
    }

    @Test
    void updateFolder_shouldUpdateFolderSuccessfully() {
        // Given
        DocumentFolder existingFolder = new DocumentFolder();
        existingFolder.setId(1L);
        existingFolder.setName("Old Name");
        existingFolder.setProjectId(1L);
        existingFolder.setCreatorId(1L);
        existingFolder.setDeleted(false);

        DocumentFolder updatedFolder = new DocumentFolder();
        updatedFolder.setName("New Name");

        when(documentFolderMapper.selectById(1L)).thenReturn(existingFolder);
        when(documentFolderMapper.updateById(any(DocumentFolder.class))).thenReturn(1);

        // When
        documentFolderService.updateFolder(1L, updatedFolder);

        // Then
        verify(documentFolderMapper, times(1)).selectById(1L);
        verify(documentFolderMapper, times(1)).updateById(any(DocumentFolder.class));
    }

    @Test
    void deleteFolder_shouldDeleteFolderSuccessfully() {
        // Given
        DocumentFolder folder = new DocumentFolder();
        folder.setId(1L);
        folder.setName("Test Folder");
        folder.setProjectId(1L);
        folder.setCreatorId(1L);
        folder.setDeleted(false);

        when(documentFolderMapper.selectById(1L)).thenReturn(folder);
        when(documentFolderMapper.deleteById(1L)).thenReturn(1);

        // When
        documentFolderService.deleteFolder(1L);

        // Then
        verify(documentFolderMapper, times(1)).selectById(1L);
        verify(documentFolderMapper, times(1)).deleteById(1L);
    }
}