package com.example.DocumentService.DocumentDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDocumentDTO {
    private UUID docId;
    private UUID ownerId;
    private String title;
    private Object content;
}