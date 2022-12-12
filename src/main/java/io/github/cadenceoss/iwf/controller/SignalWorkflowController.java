package io.github.cadenceoss.iwf.controller;

import io.github.cadenceoss.iwf.core.Client;
import io.github.cadenceoss.iwf.core.WorkflowStartOptions;
import io.github.cadenceoss.iwf.workflow.persistence.BasicPersistenceWorkflow;
import io.github.cadenceoss.iwf.workflow.persistence.BasicPersistenceWorkflowState1;
import io.github.cadenceoss.iwf.workflow.signal.BasicSignalWorkflow;
import io.github.cadenceoss.iwf.workflow.signal.BasicSignalWorkflowState1;
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
        final WorkflowStartOptions startOptions = WorkflowStartOptions.minimum(10);
        final Integer input = 1;
        final String runId = client.startWorkflow(
                BasicSignalWorkflow.class, BasicSignalWorkflowState1.STATE_ID, input, wfId, startOptions);
        client.signalWorkflow(
                BasicSignalWorkflow.class, wfId, runId, BasicSignalWorkflowState1.SIGNAL_CHANNEL_NAME_1, Integer.valueOf(2));
        final Integer output = client.getSimpleWorkflowResultWithWait(Integer.class, wfId);

        return ResponseEntity.ok(String.format("runId: %s, output: %d", runId, output));
    }
}