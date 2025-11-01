package com.promanage.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.promanage.common.result.PageResult;
import com.promanage.service.dto.request.DocumentSearchRequest;
import com.promanage.service.entity.Document;
import com.promanage.service.mapper.CommentMapper;
import com.promanage.service.service.IDocumentFolderService;
import com.promanage.service.service.IDocumentService;
import com.promanage.service.service.ITagService;
import com.promanage.service.service.IUserService;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

  @Mock private IDocumentService documentService;
  @Mock private IDocumentFolderService documentFolderService;
  @Mock private IUserService userService;
  @Mock private CommentMapper commentMapper;
  @Mock private ITagService tagService;

  @InjectMocks private DocumentController documentController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void searchDocuments_shouldReturnDocuments() throws Exception {
    PageResult<Document> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 20);
    when(documentService.searchDocuments(any(DocumentSearchRequest.class), anyLong()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/documents/search")
                .param("page", "1")
                .param("size", "20")
                .param("projectId", "1")
                .param("status", "2")
                .param("keyword", "test")
                .param("folderId", "3")
                .param("creatorId", "4")
                .param("type", "PRD")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authenticatedUser(100L, "document:search")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("success"));
  }

  @Test
  void searchDocuments_withTimeRange_shouldReturnDocuments() throws Exception {
    PageResult<Document> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 20);
    when(documentService.searchDocuments(any(DocumentSearchRequest.class), anyLong()))
        .thenReturn(pageResult);

    mockMvc
        .perform(
            get("/api/v1/documents/search")
                .param("page", "1")
                .param("size", "20")
                .param("startTime", "2023-01-01T00:00:00")
                .param("endTime", "2023-12-31T23:59:59")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authenticatedUser(100L, "document:search")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("success"));
  }

  private RequestPostProcessor authenticatedUser(Long userId, String... authorities) {
    return request -> {
      List<GrantedAuthority> grantedAuthorities = new java.util.ArrayList<>();
      for (String authority : authorities) {
        grantedAuthorities.add(new SimpleGrantedAuthority(authority));
      }
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken("mockUser", "password", grantedAuthorities);
      authenticationToken.setDetails(Collections.singletonMap("userId", userId));
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      request.setUserPrincipal(authenticationToken);
      return request;
    };
  }
}
