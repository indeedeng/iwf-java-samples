package io.iworkflow.controller;

import io.iworkflow.models.SignalRequest;
import io.iworkflow.models.StartWorkflowResponse;
import io.iworkflow.services.DynamicWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dsl")
public class DslWorkflowController {

    private final DynamicWorkflowService dynamicWorkflowService;

    public DslWorkflowController(
            final DynamicWorkflowService dynamicWorkflowService
    ) {
        this.dynamicWorkflowService = dynamicWorkflowService;
    }

    @GetMapping("/workflows")
    public ResponseEntity<List<String>> getWorkflowTypes() {
        return ResponseEntity.ok(dynamicWorkflowService.getDynamicWorkflowTypes());
    }

    @PostMapping("/workflows/{workflowName}/start")
    public ResponseEntity<StartWorkflowResponse> startWorkflowWithName(
            @PathVariable final String workflowName
    ) {
        return ResponseEntity.ok(dynamicWorkflowService.startWorkflow(workflowName));
    }

    @PostMapping("/workflows/{workflowName}/{workflowId}/{runId}/signal")
    public ResponseEntity<Void> startWorkflowWithName(
            @PathVariable final String workflowName,
            @PathVariable final String workflowId,
            @PathVariable final String runId,
            @RequestBody final SignalRequest signalRequest
    ) {
        dynamicWorkflowService.signalWorkflow(workflowName, workflowId, runId, signalRequest);
        return ResponseEntity.ok().build();
    }
}