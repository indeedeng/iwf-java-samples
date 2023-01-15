package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.workflow.basic.BasicWorkflow;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/basic")
public class BasicWorkflowController {

    private final Client client;

    public BasicWorkflowController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/start")
    public ResponseEntity<String> start() {
        final String wfId = "basic-test-id" + System.currentTimeMillis() / 1000;
        final Integer input = 0;
        final String runId = client.startWorkflow(BasicWorkflow.class, wfId, 10, input);

        final Integer output = client.getSimpleWorkflowResultWithWait(Integer.class, wfId);

        return ResponseEntity.ok(String.format("runId: %s, output: %d", runId, output));
    }
}