package io.iworkflow.config;

import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.Registry;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IwfWorkflowRegistrationProcessor implements SmartInitializingSingleton {
    private final Registry workflowRegistry;
    private final List<ObjectWorkflow> workflows;

    public IwfWorkflowRegistrationProcessor(final Registry workflowRegistry, final List<ObjectWorkflow> workflows) {
        this.workflowRegistry = workflowRegistry;
        this.workflows = workflows;
    }

    @Override
    public void afterSingletonsInstantiated() {
        workflows.forEach(workflowRegistry::addWorkflow);
    }
}