package com.eugene.book_service.kafka;

import com.eugene.book_service.dto.event.BookDtoEvent;
import com.eugene.book_service.exception.JsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BookEventProducer
{

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BookEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBookDeletedEvent(Set<Long> reviewsIds) {
        try {
            String json = this.objectMapper.writeValueAsString(
                    new BookDtoEvent(KafkaEventType.BOOK_DELETED, reviewsIds));
            this.kafkaTemplate.send("book.events", json);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize the list of IDs", e.getCause());
        }
    }
}
