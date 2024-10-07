package io.iworkflow.workflow.shortlistcandidates.utils;

import io.iworkflow.core.Client;
import io.iworkflow.core.exceptions.WorkflowAlreadyStartedException;
import io.iworkflow.workflow.shortlistcandidates.EmployerOptInWorkflow;

public class WorkflowUtil {
    public static String buildEmployerOptInWorkflowId(final String employerId) {
        return "shortlist_candidates_opt_in_" + employerId;
    }

    public static String buildShortlistWorkflowId(final String employerId, final String candidateId) {
        return "shortlist_candidates_shortlist_" + employerId + "_" + candidateId;
    }

    public static Boolean isOptedIn(final Client client, final String employerId) {
        final String workflowId = buildEmployerOptInWorkflowId(employerId);

        final EmployerOptInWorkflow rpcStub = client.newRpcStub(
                EmployerOptInWorkflow.class,
                workflowId
        );

        try {
            return client.invokeRPC(rpcStub::isOptedIn);
        } catch (final WorkflowAlreadyStartedException e) {
            return false;
        }
    }
}
