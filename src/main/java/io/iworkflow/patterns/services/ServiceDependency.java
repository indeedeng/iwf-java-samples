package io.iworkflow.patterns.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ServiceDependency {
    private int readExternalCounter = 0;

    /**
     * Attempts to call an external API with the provided message.
     *
     * <p>This method simulates an external system call that may fail initially.
     * It will throw a RuntimeException on the first two attempts to simulate
     * transient errors that require retrying. On the third attempt, it will
     * succeed and return a result.
     *
     * <p>Use this method in conjunction with an appropriate {@code RetryPolicy}.
     *
     * @param message the message to be sent to the external system.
     * @return a string representing the result from the external system.
     * @throws RuntimeException if the call fails due to an error in the external system.
     */
    public String attemptExternalApiCall(final String message) throws RuntimeException {
        System.out.printf("Try external system call: (%s)%n", readExternalCounter);
        if (readExternalCounter++ < 2) {
            throw new RuntimeException("There is an error when calling external system, retry it");
        }

        readExternalCounter = 0;
        System.out.printf("Data read from external system: (%s)%n", message);
        return "External data result";
    }

    public String externalApiCall(final String message) throws RuntimeException {
        System.out.printf("Data read from external system: (%s)%n", message);
        return "External data result";
    }

    public void updateExternalSystem(final String message) {
        System.out.printf("update external system(like sending Kafka message or upsert to database): %s%n", message);
    }

    public void sendEmail(final String subject, final String content) {
        System.out.printf("send an email to job seeker, title: %s, content: %s %n", subject, content);
    }

    public void upsert(final Object document) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final String serializedObject = objectMapper.writeValueAsString(document);
        System.out.printf("upsert: %s %n", serializedObject);
    }
}
