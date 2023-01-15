package io.iworkflow.config;

import io.iworkflow.workflow.dsl.DynamicDslWorkflow;
import io.iworkflow.workflow.dsl.utils.DynamicDslWorkflowAdapter;
import io.serverlessworkflow.api.Workflow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class DslWorkflowConfig {
    // TODO need to fix
    // @Bean
    public io.iworkflow.core.Workflow dynamicWorkflow(final Map<String, DynamicDslWorkflowAdapter> adapterMap) {
        return new DynamicDslWorkflow(adapterMap);
    }

    @Bean
    public Map<String, DynamicDslWorkflowAdapter> workflowFactoryMap(final Map<String, Workflow> workflowMap) {
        return workflowMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new DynamicDslWorkflowAdapter(e.getValue())));
    }
}
