package io.iworkflow.controller;

import io.iworkflow.core.Client;
import io.iworkflow.core.ClientSideException;
import io.iworkflow.gen.models.ErrorSubStatus;
import io.iworkflow.workflow.shortlistcandidates.EmployerOptInWorkflow;
import io.iworkflow.workflow.shortlistcandidates.ShortlistWorkflow;
import io.iworkflow.workflow.shortlistcandidates.model.ImmutableEmployerOptInInput;
import io.iworkflow.workflow.shortlistcandidates.model.ImmutableShortlistInput;
import io.iworkflow.workflow.shortlistcandidates.utils.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;


@Controller
@RequestMapping("/shortlist_candidates")
public class ShortlistCandidatesController {

    @Autowired
    private Client client;

    @PostMapping("/opt_in")
    public ResponseEntity<String> optIn(
            @RequestBody Map<String, Object> requestBody
    ) {
        final String employerId = requestBody.get("employerId").toString();

        final String workflowId = WorkflowUtil.buildEmployerOptInWorkflowId(employerId);

        final ImmutableEmployerOptInInput input = ImmutableEmployerOptInInput.builder()
                .employerId(employerId)
                .build();

        try {
            // The timeout is set to 0, indicating that the workflow will never time out
            client.startWorkflow(EmployerOptInWorkflow.class, workflowId, 0, input);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                return ResponseEntity.ok(String.format("Employer %s has already opted in", employerId));
            }
            throw new RuntimeException(String.format("optIn failed with workflow %s", workflowId), e);
        }

        return ResponseEntity.ok(String.format("Started workflowId: %s", workflowId));
    }

    @PostMapping("/opt_out")
    public ResponseEntity<String> optOut(
            @RequestBody Map<String, Object> requestBody
    ) {
        final String employerId = requestBody.get("employerId").toString();

        final String workflowId = WorkflowUtil.buildEmployerOptInWorkflowId(employerId);

        final EmployerOptInWorkflow rpcStub = client.newRpcStub(
                EmployerOptInWorkflow.class,
                workflowId,
                ""
        );

        try {
            client.invokeRPC(rpcStub::optOut);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_NOT_EXISTS_SUB_STATUS) {
                return ResponseEntity.ok(String.format("Employer %s is not in the opt-in status", employerId));
            }
            throw new RuntimeException(String.format("optOut failed with workflow %s", workflowId), e);
        }

        return ResponseEntity.ok(String.format("Employer %s has opted out", employerId));
    }

    @GetMapping("/is_opted_in")
    public ResponseEntity<Boolean> isOptedIn(
            @RequestParam(defaultValue = "test-employer") String employerId
    ) {
        final Boolean employerOptInStatus = WorkflowUtil.isOptedIn(client, employerId);
        return ResponseEntity.ok(employerOptInStatus);
    }

    @PostMapping("/shortlist")
    public ResponseEntity<String> shortlist(
            @RequestBody Map<String, Object> requestBody
    ) {
        final String employerId = requestBody.get("employerId").toString();
        final String candidateId = requestBody.get("candidateId").toString();

        // Check whether the employer has opted in
        final Boolean isOptedIn = WorkflowUtil.isOptedIn(client, employerId);
        if (!isOptedIn) {
            return ResponseEntity.ok(
                    String.format("Do nothing for %s because of no opt-in", employerId + "-" + candidateId)
            );
        }

        final String workflowId = WorkflowUtil.buildShortlistWorkflowId(employerId, candidateId);

        final ImmutableShortlistInput input = ImmutableShortlistInput.builder()
                .employerId(employerId)
                .candidateId(candidateId)
                .build();

        try {
            // Set the timeout to 8 minutes because there is a 5-minute window before sending the email
            client.startWorkflow(ShortlistWorkflow.class, workflowId, 8 * 3600, input);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_ALREADY_STARTED_SUB_STATUS) {
                return ResponseEntity.ok(String.format("Already running workflowId: %s", workflowId));
            }
            throw new RuntimeException(String.format("Failed to start the workflow %s", workflowId), e);
        }

        return ResponseEntity.ok(String.format("Started workflowId: %s", workflowId));
    }

    @PostMapping("/revoke_shortlist")
    public ResponseEntity<String> revokeShortlist(
            @RequestBody Map<String, Object> requestBody
    ) {
        final String employerId = requestBody.get("employerId").toString();
        final String candidateId = requestBody.get("candidateId").toString();

        final String workflowId = WorkflowUtil.buildShortlistWorkflowId(employerId, candidateId);

        try {
            client.signalWorkflow(ShortlistWorkflow.class, workflowId, "", ShortlistWorkflow.SIGNAL_REVOKE_SHORTLIST, null);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_NOT_EXISTS_SUB_STATUS) {
                return ResponseEntity.ok(String.format("No running workflow to revoke for %s", employerId + "-" + candidateId));
            }
            throw new RuntimeException(String.format("Failed to signal with workflow %s", workflowId), e);
        }

        return ResponseEntity.ok(String.format("Revoked shortlist for %s", employerId + "-" + candidateId));
    }

    @GetMapping("/email_sent_timestamp")
    public ResponseEntity<Long> getEmailSentTimestamp(
            @RequestParam(defaultValue = "test-employer") String employerId,
            @RequestParam(defaultValue = "test-candidate") String candidateId
    ) {
        final String workflowId = WorkflowUtil.buildShortlistWorkflowId(employerId, candidateId);

        Long timestamp;
        try {
            final ShortlistWorkflow rpcStub = client.newRpcStub(
                    ShortlistWorkflow.class,
                    workflowId,
                    ""
            );
            timestamp = client.invokeRPC(rpcStub::getEmailSentTimestamp);
        } catch (final ClientSideException e) {
            if (e.getErrorSubStatus() == ErrorSubStatus.WORKFLOW_NOT_EXISTS_SUB_STATUS) {
                timestamp = 0L;
            } else {
                throw new RuntimeException(String.format("getEmailSentTimestamp failed with workflow %s", workflowId), e);
            }
        }
        return ResponseEntity.ok(timestamp);
    }
}
