package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.workflow.subscription.SubscriptionWorkflow;
import io.iworkflow.workflow.subscription.model.Customer;
import io.iworkflow.workflow.subscription.model.ImmutableCustomer;
import io.iworkflow.workflow.subscription.model.ImmutableSubscription;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;

import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.signalCancelSubscription;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.signalUpdateBillingPeriodCharge;

@Controller
@RequestMapping("/subscription")
public class SubscriptionWorkflowController {

    private final Client client;

    public SubscriptionWorkflowController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/start")
    public ResponseEntity<String> start() {
        final String wfId = "subscription-test-id" + System.currentTimeMillis() / 1000;
        final Customer customer = ImmutableCustomer.builder()
                .firstName("Quanzheng")
                .lastName("Long")
                .id("qlong")
                .email("qlong.seattle@gmail.com")
                .subscription(
                        ImmutableSubscription.builder()
                                .trialPeriod(Duration.ofSeconds(20))
                                .billingPeriod(Duration.ofSeconds(10))
                                .maxBillingPeriods(10)
                                .billingPeriodCharge(100)
                                .build()
                )
                .build();
        final String runId = client.startWorkflow(SubscriptionWorkflow.class, wfId, 3600, customer);

        return ResponseEntity.ok(String.format("workflowId: %s: runId: %s", wfId, runId));
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> cancel(
            @RequestParam String workflowId
    ) {
        client.signalWorkflow(SubscriptionWorkflow.class, workflowId, "", signalCancelSubscription, true);
        return ResponseEntity.ok("done");
    }

    @GetMapping("/updateChargeAmount")
    public ResponseEntity<String> updateChargeAmount(
            @RequestParam String workflowId,
            @RequestParam int newChargeAmount
    ) {
        client.signalWorkflow(SubscriptionWorkflow.class, workflowId, "", signalUpdateBillingPeriodCharge, newChargeAmount);
        return ResponseEntity.ok("done");
    }
}