package io.iworkflow.workflow;

import org.springframework.stereotype.Component;

/**
 * This is to demonstrate how to depend on other services in the workflow code
 */
@Component
public class MyDependencyService {

    private int updateExternalCounter = 0;

    public void updateExternalSystem(final String message) {
        if (updateExternalCounter++ <= 2) {
            throw new RuntimeException("hey this is an error when calling external system, you should retry it");
        } else {
            updateExternalCounter = 0;
        }
        System.out.printf("update external system(like via RPC, or sending Kafka message or database): %s", message);
    }

    public void sendEmail(final String recipient, final String subject, final String content) {
        System.out.printf("send an email to %s, title: %s, content: %s %n", recipient, subject, content);
    }

    public void chargeUser(final String email, final String customerId, final int amount) {
        System.out.printf("charge user customerId[%s] email[%s] for $%d \n", customerId, email, amount);
    }

    public void callAPI1(String someData) {
        System.out.println("external API#1 is called");
    }

    public void callAPI2(String someData) {
        System.out.println("external API#2 is called");
    }

    public void callAPI3(String someData) {
        System.out.println("external API#3 is called");
    }

    public void callAPI4(String someData) {
        System.out.println("external API#4 is called");
    }
}
