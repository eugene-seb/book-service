package com.eugene.book_service.kafka;

import com.eugene.book_service.dto.event.BookEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BookEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBookDeletedEvent(Set<Long> reviewsIds) {
        kafkaTemplate.send("book.events", new BookEvent("BOOK_DELETED", reviewsIds));
    }
}
