package io.github.cadenceoss.iwf.controller;

import io.github.cadenceoss.iwf.core.WorkerService;
import io.github.cadenceoss.iwf.core.WorkflowStartOptions;
import io.github.cadenceoss.iwf.gen.models.WorkflowSignalRequest;
import io.github.cadenceoss.iwf.gen.models.WorkflowStateDecideRequest;
import io.github.cadenceoss.iwf.gen.models.WorkflowStateDecideResponse;
import io.github.cadenceoss.iwf.gen.models.WorkflowStateStartRequest;
import io.github.cadenceoss.iwf.gen.models.WorkflowStateStartResponse;
import io.github.cadenceoss.iwf.models.SignalRequest;
import io.github.cadenceoss.iwf.models.StartWorkflowResponse;
import io.github.cadenceoss.iwf.services.DynamicWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/v1")
public class ApiController {

    private final WorkerService workerService;
    private final DynamicWorkflowService dynamicWorkflowService;

    public ApiController(
            final WorkerService workerService,
            final DynamicWorkflowService dynamicWorkflowService
    ) {
        this.workerService = workerService;
        this.dynamicWorkflowService = dynamicWorkflowService;
    }

    @PostMapping("/workflowState/start")
    public ResponseEntity<WorkflowStateStartResponse> apiV1WorkflowStateStartPost(
            final @RequestBody WorkflowStateStartRequest request
    ) {
        WorkflowStateStartResponse body = workerService.handleWorkflowStateStart(request);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/workflowState/decide")
    public ResponseEntity<WorkflowStateDecideResponse> apiV1WorkflowStateDecidePost(
            final @RequestBody WorkflowStateDecideRequest request
    ) {
        return ResponseEntity.ok(workerService.handleWorkflowStateDecide(request));
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