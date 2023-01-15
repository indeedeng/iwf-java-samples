package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.workflow.signal.BasicSignalWorkflow;
import io.iworkflow.workflow.signal.BasicSignalWorkflowState1;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/signal")
public class SignalWorkflowController {

    private final Client client;

    public SignalWorkflowController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/start")
    public ResponseEntity<String> start() {
        final String wfId = "basic-signal-test-id" + System.currentTimeMillis() / 1000;
        final Integer input = 1;
        final String runId = client.startWorkflow(
                BasicSignalWorkflow.class, wfId, 10, input);
        client.signalWorkflow(
                BasicSignalWorkflow.class, wfId, runId, BasicSignalWorkflowState1.SIGNAL_CHANNEL_NAME_1, Integer.valueOf(2));
        final Integer output = client.getSimpleWorkflowResultWithWait(Integer.class, wfId);

        return ResponseEntity.ok(String.format("runId: %s, output: %d", runId, output));
    }
}