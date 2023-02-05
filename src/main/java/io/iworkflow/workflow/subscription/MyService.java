package io.iworkflow.workflow.subscription;

import org.springframework.stereotype.Component;

@Component
public class MyService {
    public void sendEmail(final String recipient, final String subject, final String content) {
        System.out.printf("sending an welcome email to %s, title: %s, content: %s %n", recipient, subject, content);
    }

    public void chargeUser(final String email, final String customerId, final int amount) {
        System.out.printf("charge user customerId[%s] email[%s] for $%d \n", customerId, email, amount);
    }
}
