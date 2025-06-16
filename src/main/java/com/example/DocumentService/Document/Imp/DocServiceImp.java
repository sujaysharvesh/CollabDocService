package com.example.DocumentService.Document.Imp;

import com.example.DocumentService.Document.DocMapper;
import com.example.DocumentService.Document.DocRepository;
import com.example.DocumentService.Document.Document;
import com.example.DocumentService.Document.DocService;
import com.example.DocumentService.Document.DocumentDTO.*;
import com.example.DocumentService.Document.Exceptionhandler.GlobalExceptionHandler.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@AllArgsConstructor
public class DocServiceImp implements DocService {

    private final DocRepository docRepository;
    private final DocMapper docMapper;

    @Override
    public DocumentResponseDTO createDocument(CreateDocumentDTO createDocumentDTO) {
        Document document = Document.builder()
                .title(createDocumentDTO.getTitle())
                .content(createDocumentDTO.getContent())
                .ownerId(createDocumentDTO.getOwnerId())
                .build();
        Document newDoc = docRepository.save(document);
        return docMapper.toDocumentResponseDTO(newDoc);
    }

    @Override
    public DocumentResponseDTO updateDocument(UpdateDocumentDTO updateDocumentDTO) {
        canUserAccessDocument(updateDocumentDTO.getDocId(), updateDocumentDTO.getOwnerId());
        Document document = docRepository.findById(updateDocumentDTO.getDocId())
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        document.setTitle(updateDocumentDTO.getTitle());
        document.setContent(updateDocumentDTO.getContent());
        Document updatedDoc = docRepository.save(document);
        return docMapper.toDocumentResponseDTO(updatedDoc);
    }

    @Override
    public List<UserDocumentsDTO> getUserAllDocuments(UUID ownerId) {
        List<Document> userDocuments = docRepository.findByOwnerId(ownerId);
        return userDocuments.stream()
                .map(document ->
                        UserDocumentsDTO.builder()
                                .docId(document.getId())
                                .title(document.getTitle()).build()).toList();
    }

    @Override
    public DocumentResponseDTO getUserDocument(UUID docId, UUID ownerId) {
        Document document = docRepository.findByIdAndOwnerId(docId, ownerId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found or you do not have permission to access it."));
        return docMapper.toDocumentResponseDTO(document);
    }

    @Override
    public void deleteDocument(UUID docId, UUID ownerId) {
        canUserAccessDocument(docId, ownerId);
        Document document = docRepository.findById(docId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        docRepository.delete(document);
    }

    @Override
    public void updateDocumentContent(DocumentSaveEvent documentSaveEvent) {
        UUID userId = UUID.fromString(documentSaveEvent.getUserId());
        UUID documentId = UUID.fromString(documentSaveEvent.getDocumentId());
        canUserAccessDocument(documentId, userId);
        Document document = docRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));
        document.setUpdatedAt(documentSaveEvent.getTimestamp());
        document.setContent(documentSaveEvent.getContent());
        docRepository.save(document);
    }


    public void canUserAccessDocument(UUID docId, UUID userId) {
        if (!docRepository.existsByIdAndOwnerId(docId, userId))
            throw new DocumentAccessDeniedException("Document not found or you do not have permission to access it.");

    }
}
