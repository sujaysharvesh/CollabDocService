package com.example.DocumentService.Document;

import com.example.DocumentService.Document.DocumentDTO.*;

import java.util.List;
import java.util.UUID;

public interface DocService {

    DocumentResponseDTO createDocument(CreateDocumentDTO createDocumentDTO, UUID userId);
    DocumentResponseDTO updateDocument(UpdateDocumentDTO updateDocumentDTO);
    List<UserDocumentsDTO> getUserAllDocuments(UUID ownerId);
    DocumentResponseDTO getUserDocument(UUID docId, UUID ownerId);
    void deleteDocument(UUID docId, UUID ownerId);
    public void updateDocumentContent(DocumentSaveEvent documentSaveEvent);
}
