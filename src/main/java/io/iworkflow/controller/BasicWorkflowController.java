package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.ImmutableWorkflowOptions;
import io.iworkflow.core.WorkflowOptions;
import io.iworkflow.gen.models.WorkflowStartOptions;
import io.iworkflow.workflow.basic.BasicWorkflow;
import io.iworkflow.workflow.basic.BasicWorkflowS1;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

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
        final WorkflowOptions startOptions = ImmutableWorkflowOptions.builder()
                .workflowTimeoutSeconds(10)
                .workflowIdReusePolicy(Optional.of(WorkflowStartOptions.WorkflowIDReusePolicyEnum.ALLOW_DUPLICATE))
                .build();
        final Integer input = 0;
        final String runId = client.startWorkflow(BasicWorkflow.class, BasicWorkflowS1.StateId, input, wfId, startOptions);

        final Integer output = client.getSimpleWorkflowResultWithWait(Integer.class, wfId);

        return ResponseEntity.ok(String.format("runId: %s, output: %d", runId, output));
    }
}