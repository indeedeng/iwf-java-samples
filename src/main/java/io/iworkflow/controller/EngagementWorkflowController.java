package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.workflow.engagement.EngagementWorkflow;
import io.iworkflow.workflow.engagement.model.EngagementDescription;
import io.iworkflow.workflow.engagement.model.EngagementInput;
import io.iworkflow.workflow.engagement.model.ImmutableEngagementInput;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/engagement")
public class EngagementWorkflowController {

    private final Client client;

    public EngagementWorkflowController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/start")
    public ResponseEntity<String> start() {
        final String wfId = "engagement_test_id_" + System.currentTimeMillis() / 1000;
        final EngagementInput input = ImmutableEngagementInput.builder()
                .proposeUserId("test-proposer")
                .targetUserId("test-target-user")
                .notes("test-notes")
                .build();
        final String runId = client.startWorkflow(EngagementWorkflow.class, wfId, 3600, input);

        return ResponseEntity.ok(String.format("started workflowId: %s", wfId));
    }

    @GetMapping("/optout")
    public ResponseEntity<String> optout(
            @RequestParam String workflowId
    ) {
        client.signalWorkflow(EngagementWorkflow.class, workflowId, "", EngagementWorkflow.SIGNAL_NAME_OPT_OUT_REMINDER, null);
        return ResponseEntity.ok("done");
    }

    @GetMapping("/decline")
    public ResponseEntity<String> decline(
            @RequestParam String workflowId,
            @RequestParam String notes
    ) {
        final EngagementWorkflow rpcStub = client.newRpcStub(EngagementWorkflow.class, workflowId, "");
        client.invokeRPC(rpcStub::decline, notes);

        return ResponseEntity.ok("declined");
    }

    @GetMapping("/describe")
    public ResponseEntity<EngagementDescription> describe(
            @RequestParam String workflowId
    ) {
        final EngagementWorkflow rpcStub = client.newRpcStub(EngagementWorkflow.class, workflowId, "");
        final EngagementDescription description = client.invokeRPC(rpcStub::describe);

        return ResponseEntity.ok(description);
    }

    @GetMapping("/accept")
    public ResponseEntity<String> accept(
            @RequestParam String workflowId,
            @RequestParam String notes
    ) {
        final EngagementWorkflow rpcStub = client.newRpcStub(EngagementWorkflow.class, workflowId, "");
        client.invokeRPC(rpcStub::accept, notes);

        return ResponseEntity.ok("accepted");
    }
}