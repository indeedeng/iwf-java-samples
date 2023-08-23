package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.ClientSideException;
import io.iworkflow.gen.models.ErrorSubStatus;
import io.iworkflow.workflow.microservices.OrchestrationWorkflow;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/microservice")
public class MicroserviceWorkflowController {

    private final Client client;

    public MicroserviceWorkflowController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/start")
    public ResponseEntity<String> start(
            @RequestParam String workflowId
    ) {
        try {
            client.startWorkflow(OrchestrationWorkflow.class, workflowId, 3600, "some input data, could be any object rather than a string");
        } catch (ClientSideException e) {
            if (e.getErrorSubStatus() != ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                throw e;
            }
        }
        return ResponseEntity.ok("success");
    }

    @GetMapping("/signal")
    ResponseEntity<String> receiveSignalForApiOrchestration(
            @RequestParam String workflowId) {
        client.signalWorkflow(OrchestrationWorkflow.class, workflowId, "", OrchestrationWorkflow.READY_SIGNAL, null);
        return ResponseEntity.ok("done");
    }

    @GetMapping("/swap")
    ResponseEntity<String> swapData(
            @RequestParam String workflowId,
            @RequestParam String data) {
        final OrchestrationWorkflow rpcStub = client.newRpcStub(OrchestrationWorkflow.class, workflowId);
        String oldData = client.invokeRPC(rpcStub::swap, data);
        return ResponseEntity.ok("oldData: " + oldData);
    }
}