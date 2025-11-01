package com.promanage.service.service;

import java.util.List;

public interface IDocumentTagService {
  void setDocumentTags(Long documentId, List<Long> tagIds);

  List<Long> getDocumentTagIds(Long documentId);
}
