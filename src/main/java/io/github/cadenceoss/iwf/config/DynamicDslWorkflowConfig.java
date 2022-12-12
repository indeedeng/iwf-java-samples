package io.github.cadenceoss.iwf.config;

import io.github.cadenceoss.iwf.dsl.DynamicDslWorkflow;
import io.github.cadenceoss.iwf.dsl.utils.DynamicDslWorkflowAdapter;
import io.serverlessworkflow.api.Workflow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class DynamicDslWorkflowConfig {
    @Bean
    public io.github.cadenceoss.iwf.core.Workflow dynamicWorkflow(final Map<String, DynamicDslWorkflowAdapter> adapterMap) {
        return new DynamicDslWorkflow(adapterMap);
    }

    @Bean
    public Map<String, DynamicDslWorkflowAdapter> workflowFactoryMap(final Map<String, Workflow> workflowMap) {
        return workflowMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new DynamicDslWorkflowAdapter(e.getValue())));
    }
}
