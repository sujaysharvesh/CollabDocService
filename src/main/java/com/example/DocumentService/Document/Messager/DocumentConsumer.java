package com.example.DocumentService.Document.Messager;


import com.example.DocumentService.Document.DocService;
import com.example.DocumentService.Document.DocumentDTO.DocumentSaveEvent;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentConsumer {

    @Autowired
    private DocService docService;

    @RabbitListener(queues = "document.save.queue")
    public void updateDocument(DocumentSaveEvent event) {
        docService.updateDocumentContent(event);
    }

}
