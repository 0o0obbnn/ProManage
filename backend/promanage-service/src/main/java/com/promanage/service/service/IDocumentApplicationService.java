package com.promanage.service.service;

import com.promanage.service.dto.response.DocumentDetailDTO;

/**
 * Application service for handling complex document-related use cases.
 * <p>
 * This service acts as a facade, orchestrating calls to multiple domain services
 * to assemble complex DTOs for the API layer, such as the detailed document view.
 * </p>
 *
 * @author ProManage Team
 * @date 2025-10-12
 */
public interface IDocumentApplicationService {

    /**
     * Gets all the details for a document, optimized to prevent N+1 queries.
     *
     * @param documentId The ID of the document.
     * @return A DocumentDetailDTO containing the aggregated data.
     */
    DocumentDetailDTO getDocumentDetails(Long documentId);

    /**
     * Creates a new document and returns its detailed response.
     *
     * @param projectId The ID of the project where the document will be created.
     * @param request   The request object containing document creation details.
     * @return A DocumentDetailDTO containing the newly created document's aggregated data.
     */
    DocumentDetailDTO createDocument(Long projectId, com.promanage.service.dto.request.CreateDocumentRequest request);

}
