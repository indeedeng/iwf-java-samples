package io.github.cadenceoss.iwf.controller;

import io.github.cadenceoss.iwf.core.Client;
import io.github.cadenceoss.iwf.core.ImmutableWorkflowStartOptions;
import io.github.cadenceoss.iwf.core.WorkflowStartOptions;
import io.github.cadenceoss.iwf.core.options.WorkflowIdReusePolicy;
import io.github.cadenceoss.iwf.workflow.basic.BasicWorkflow;
import io.github.cadenceoss.iwf.workflow.basic.BasicWorkflowS1;
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
        final WorkflowStartOptions startOptions = ImmutableWorkflowStartOptions.builder()
                .workflowTimeoutSeconds(10)
                .workflowIdReusePolicy(Optional.of(WorkflowIdReusePolicy.ALLOW_DUPLICATE))
                .build();
        final Integer input = 0;
        final String runId = client.startWorkflow(BasicWorkflow.class, BasicWorkflowS1.StateId, input, wfId, startOptions);

        final Integer output = client.getSimpleWorkflowResultWithWait(Integer.class, wfId);

        return ResponseEntity.ok(String.format("runId: %s, output: %d", runId, output));
    }
}