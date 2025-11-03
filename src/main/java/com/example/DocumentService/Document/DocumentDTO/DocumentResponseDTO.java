package com.example.DocumentService.Document.DocumentDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponseDTO {

    private String documentId;

    private String title;

    @JsonProperty("content")
    public String getContent() {
        if (content == null || content.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(content);
    }

    @JsonProperty("content")
    public void setContent(String base64Content) {
        if (base64Content == null || base64Content.isEmpty()) {
            this.content = new byte[0];
        } else {
            this.content = Base64.getDecoder().decode(base64Content);
        }
    }


    private byte[] content;

    @JsonProperty("updated_at")
    private Instant updatedAt;

}