package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.WorkflowStartOptions;
import io.iworkflow.workflow.interstatechannel.BasicInterStateChannelWorkflow;
import io.iworkflow.workflow.interstatechannel.BasicInterStateChannelWorkflowState0;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/interstateChannel")
public class InterstateChannelWorkflowController {

    private final Client client;

    public InterstateChannelWorkflowController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/start")
    public ResponseEntity<String> start() {
        final String wfId = "basic-inter-state-test-id" + System.currentTimeMillis() / 1000;
        final WorkflowStartOptions startOptions = WorkflowStartOptions.minimum(10);
        final Integer input = 1;
        final String runId = client.startWorkflow(
                BasicInterStateChannelWorkflow.class, BasicInterStateChannelWorkflowState0.STATE_ID, input, wfId, startOptions);
        final Integer output = client.getSimpleWorkflowResultWithWait(Integer.class, wfId);

        return ResponseEntity.ok(String.format("runId: %s, output: %d", runId, output));
    }
}