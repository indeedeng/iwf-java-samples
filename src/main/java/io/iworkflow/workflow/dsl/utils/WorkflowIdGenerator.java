package io.iworkflow.workflow.dsl.utils;

import io.serverlessworkflow.api.Workflow;

public class WorkflowIdGenerator {
    public static String generateDynamicWfId(final Workflow workflow) {
        return String.format("%s-%s", workflow.getId(), workflow.getVersion());
    }
}
