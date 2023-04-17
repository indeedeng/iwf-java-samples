package io.iworkflow.workflow;

import org.springframework.stereotype.Component;

/**
 * This is to demonstrate how to depend on other services in the workflow code
 */
@Component
public class MyDependencyService {

    private int notifiedExternalCounter = 0;

    public void notifyExternalSystem(final String message) {
        if (notifiedExternalCounter++ <= 2) {
            throw new RuntimeException("hey this is an error when calling external system, you should retry it");
        } else {
            notifiedExternalCounter = 0;
        }
        System.out.printf("notifying external system(like sending Kafka message): %s", message);
    }

    public void sendEmail(final String recipient, final String subject, final String content) {
        System.out.printf("send an email to %s, title: %s, content: %s %n", recipient, subject, content);
    }

    public void chargeUser(final String email, final String customerId, final int amount) {
        System.out.printf("charge user customerId[%s] email[%s] for $%d \n", customerId, email, amount);
    }
}
