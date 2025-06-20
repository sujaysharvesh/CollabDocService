package com.example.DocumentService.Document.Messager;


import com.example.DocumentService.Document.DocService;
import com.example.DocumentService.Document.DocumentDTO.DocumentSaveEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class DocumentConsumer {

    @Autowired
    private DocService docService;

    @RabbitListener(queues = "document.save.queue")
    public void updateDocument(@Payload DocumentSaveEvent event) {
        try {
            log.info("Received document save event from queue");
            log.info("Received document save event: " + event);
            System.out.println("Received document save event: " + event);
        } catch (Exception e) {
            log.error("Error processing document save event: " + e.getMessage());
            System.err.println("Error processing document save event: " + e.getMessage());
        }
        docService.updateDocumentContent(event);
    }

}
