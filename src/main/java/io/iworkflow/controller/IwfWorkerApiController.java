package io.iworkflow.controller;

import io.iworkflow.core.WorkerService;
import io.iworkflow.gen.models.WorkerErrorResponse;
import io.iworkflow.gen.models.WorkflowStateExecuteRequest;
import io.iworkflow.gen.models.WorkflowStateExecuteResponse;
import io.iworkflow.gen.models.WorkflowStateWaitUntilRequest;
import io.iworkflow.gen.models.WorkflowStateWaitUntilResponse;
import io.iworkflow.gen.models.WorkflowWorkerRpcRequest;
import io.iworkflow.gen.models.WorkflowWorkerRpcResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;

import static io.iworkflow.core.WorkerService.WORKFLOW_STATE_EXECUTE_API_PATH;
import static io.iworkflow.core.WorkerService.WORKFLOW_STATE_WAIT_UNTIL_API_PATH;
import static io.iworkflow.core.WorkerService.WORKFLOW_WORKER_RPC_API_PATH;

@Controller
@RequestMapping("/worker")
public class IwfWorkerApiController {

    private final WorkerService workerService;

    public IwfWorkerApiController(final WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostMapping(WORKFLOW_STATE_WAIT_UNTIL_API_PATH)
    public ResponseEntity<WorkflowStateWaitUntilResponse> handleWorkflowStateWaitUntil(
            final @RequestBody WorkflowStateWaitUntilRequest request
    ) {
        WorkflowStateWaitUntilResponse body = workerService.handleWorkflowStateWaitUntil(request);
        return ResponseEntity.ok(body);
    }

    @PostMapping(WORKFLOW_STATE_EXECUTE_API_PATH)
    public ResponseEntity<WorkflowStateExecuteResponse> apiV1WorkflowStateDecidePost(
            final @RequestBody WorkflowStateExecuteRequest request
    ) {
        return ResponseEntity.ok(workerService.handleWorkflowStateExecute(request));
    }

    @PostMapping(WORKFLOW_WORKER_RPC_API_PATH)
    public ResponseEntity<WorkflowWorkerRpcResponse> apiV1WorkflowStateDecidePost(
            final @RequestBody WorkflowWorkerRpcRequest request
    ) {
        return ResponseEntity.ok(workerService.handleWorkflowWorkerRpc(request));
    }

    /**
     * This exception handler will return error response to iWF server so that you can debug using Cadence/Temporal history(WebUI)
     *
     * @param req
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(
            HttpServletRequest req, Exception ex
    ) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTrace = sw.toString(); // stack trace as a string

        final WorkerErrorResponse errResp = new WorkerErrorResponse()
                .detail(ex.getMessage() + "; stack trace:" + stackTrace)
                .errorType(ex.getClass().getName());
        // TODO: you may return other status code appropriately
        int statusCode = 500;
        if (ex instanceof IllegalArgumentException) {
            statusCode = 400;
        }

        return ResponseEntity.status(statusCode).body(errResp);
    }
}