package com.example.DocumentService.Document.DocumentDTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDocumentsDTO {
    private String title;
    private UUID docId;
}
