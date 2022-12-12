package io.github.cadenceoss.iwf.controller;

import io.github.cadenceoss.iwf.core.WorkerService;
import io.github.cadenceoss.iwf.gen.models.WorkflowStateDecideRequest;
import io.github.cadenceoss.iwf.gen.models.WorkflowStateDecideResponse;
import io.github.cadenceoss.iwf.gen.models.WorkflowStateStartRequest;
import io.github.cadenceoss.iwf.gen.models.WorkflowStateStartResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;

@Controller
@RequestMapping("/api/v1")
public class IwfWorkerApiController {

    private final WorkerService workerService;

    public IwfWorkerApiController(final WorkerService workerService) {
        this.workerService = workerService;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(
            HttpServletRequest req, Exception ex
    ) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string

        return ResponseEntity.internalServerError().body(sStackTrace);
    }
}