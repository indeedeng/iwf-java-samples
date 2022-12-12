package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.WorkflowStartOptions;
import io.iworkflow.workflow.persistence.BasicPersistenceWorkflow;
import io.iworkflow.workflow.persistence.BasicPersistenceWorkflowState1;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/persistence")
public class PersistenceWorkflowController {

    private final Client client;

    public PersistenceWorkflowController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/start")
    public ResponseEntity<String> start() {
        final String wfId = "basic-persistence-test-id" + System.currentTimeMillis() / 1000;
        final WorkflowStartOptions startOptions = WorkflowStartOptions.minimum(10);
        final String runId = client.startWorkflow(
                BasicPersistenceWorkflow.class, BasicPersistenceWorkflowState1.STATE_ID, "start", wfId, startOptions);
        final String output = client.getSimpleWorkflowResultWithWait(String.class, wfId);

        return ResponseEntity.ok(String.format("runId: %s, output: %s", runId, output));
    }
}