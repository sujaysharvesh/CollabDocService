package com.example.DocumentService.Document;

import com.example.DocumentService.DocumentDTO.CreateDocumentDTO;
import com.example.DocumentService.DocumentDTO.DocumentResponseDTO;
import com.example.DocumentService.DocumentDTO.UpdateDocumentDTO;
import com.example.DocumentService.DocumentDTO.UserDocumentsDTO;

import java.util.List;
import java.util.UUID;

public interface DocService {

    DocumentResponseDTO createDocument(CreateDocumentDTO createDocumentDTO);
    DocumentResponseDTO updateDocument(UpdateDocumentDTO updateDocumentDTO);
    List<UserDocumentsDTO> getUserAllDocuments(UUID ownerId);
    DocumentResponseDTO getUserDocument(UUID docId, UUID ownerId);
    void deleteDocument(UUID docId, UUID ownerId) ;
}
