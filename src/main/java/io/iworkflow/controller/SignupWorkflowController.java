package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.ClientSideException;
import io.iworkflow.gen.models.ErrorSubStatus;
import io.iworkflow.workflow.microservices.ImmutableSignupForm;
import io.iworkflow.workflow.signup.UserSignupWorkflow;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/signup")
public class SignupWorkflowController {

    private final Client client;

    public SignupWorkflowController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/submit")
    public ResponseEntity<String> start(
            @RequestParam String username,
            @RequestParam String email
    ) {
        try {
            final ImmutableSignupForm form = ImmutableSignupForm.builder()
                    .username(username)
                    .email(email)
                    .firstName("Test")
                    .lastName("Test")
                    .build();
            client.startWorkflow(UserSignupWorkflow.class, username, 3600, form);
        } catch (ClientSideException e) {
            if (e.getErrorSubStatus() != ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                throw e;
            }
            return ResponseEntity.ok("username already started registry");
        }
        return ResponseEntity.ok("success");
    }

    @GetMapping("/verify")
    ResponseEntity<String> verify(
            @RequestParam String username) {
        final UserSignupWorkflow rpcStub = client.newRpcStub(UserSignupWorkflow.class, username);
        String result = client.invokeRPC(rpcStub::verify);
        return ResponseEntity.ok(result);
    }
}