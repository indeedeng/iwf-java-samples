package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.workflow.update.CounterWorkflow;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/counter")
public class CounterWorkflowController {

    private final Client client;

    public CounterWorkflowController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/start")
    public ResponseEntity<String> start(
            @RequestParam String id
    ) {
        client.startWorkflow(CounterWorkflow.class, id, 3600);
        return ResponseEntity.ok("success");
    }

    @GetMapping("/inc")
    ResponseEntity<Integer> verify(
            @RequestParam String id) {
        final CounterWorkflow rpcStub = client.newRpcStub(CounterWorkflow.class, id);
        return ResponseEntity.ok(client.invokeRPC(rpcStub::inc));
    }
}