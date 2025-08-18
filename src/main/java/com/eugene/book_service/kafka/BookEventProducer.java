package com.eugene.book_service.kafka;

import com.eugene.book_service.dto.event.BookDtoEvent;
import com.eugene.book_service.exception.JsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookEventProducer
{
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void sendBookDeletedEvent(Set<Long> reviewsIds) {
        try {
            String json = this.objectMapper.writeValueAsString(new BookDtoEvent(KafkaEventType.BOOK_DELETED,
                                                                                reviewsIds));
            this.kafkaTemplate.send("book.events",
                                    json);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize the list of IDs",
                                    e.getCause());
        }
    }
}
