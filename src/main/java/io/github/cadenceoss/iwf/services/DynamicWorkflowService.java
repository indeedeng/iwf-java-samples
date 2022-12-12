package io.github.cadenceoss.iwf.services;

import io.github.cadenceoss.iwf.core.UntypedClient;
import io.github.cadenceoss.iwf.core.WorkflowStartOptions;
import io.github.cadenceoss.iwf.dsl.DynamicDslWorkflow;
import io.github.cadenceoss.iwf.dsl.utils.DynamicDslWorkflowAdapter;
import io.github.cadenceoss.iwf.dsl.utils.WorkflowIdGenerator;
import io.github.cadenceoss.iwf.models.ImmutableStartWorkflowResponse;
import io.github.cadenceoss.iwf.models.SignalRequest;
import io.github.cadenceoss.iwf.models.StartWorkflowResponse;
import io.serverlessworkflow.api.interfaces.State;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DynamicWorkflowService {
    private final UntypedClient untypedClient;
    private final Map<String, DynamicDslWorkflowAdapter> adapterMap;

    public DynamicWorkflowService(final UntypedClient untypedClient, final Map<String, DynamicDslWorkflowAdapter> adapterMap) {
        this.untypedClient = untypedClient;
        this.adapterMap = adapterMap;
    }

    public StartWorkflowResponse startWorkflow(final String workflowName) {
        String workflowId = UUID.randomUUID().toString();
        DynamicDslWorkflowAdapter adapter = adapterMap.get(workflowName);
        State startState = adapter.getFirstState();

        String runId = untypedClient.startWorkflow(DynamicDslWorkflow.class.getSimpleName(),
                adapter.getWorkflow().getId() + "-" + startState.getName(),
                adapter.getFirstState(), workflowId, WorkflowStartOptions.minimum(100));
        return ImmutableStartWorkflowResponse.builder()
                .workflowId(workflowId)
                .runId(runId)
                .build();
    }

    public void signalWorkflow(final String workflowName,
                               final String workflowId,
                               final String runId,
                               final SignalRequest signalRequest) {
        untypedClient.signalWorkflow(workflowId, runId,
                adapterMap.get(workflowName).getWorkflow().getId() + "-" + signalRequest.getSignalName(),
                signalRequest.getSignal());
    }

    public List<String> getDynamicWorkflowTypes() {
        return adapterMap.values().stream()
                .map(adapter -> WorkflowIdGenerator.generateDynamicWfId(adapter.getWorkflow()))
                .collect(Collectors.toList());
    }
}
