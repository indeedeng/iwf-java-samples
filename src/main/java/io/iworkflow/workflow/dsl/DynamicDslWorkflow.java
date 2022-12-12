package io.iworkflow.workflow.dsl;

import io.iworkflow.core.StateDef;
import io.iworkflow.core.Workflow;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.persistence.DataObjectDef;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.workflow.dsl.utils.DynamicDslWorkflowAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamicDslWorkflow implements Workflow {
    private final Map<String, DynamicDslWorkflowAdapter> adapterMap;

    public DynamicDslWorkflow(final Map<String, DynamicDslWorkflowAdapter> adapterMap) {
        this.adapterMap = adapterMap;
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        Set<String> attributeDefs = adapterMap.values()
                .stream()
                .map(DynamicDslWorkflowAdapter::getStateDefsForWorkflow)
                .flatMap(Collection::stream)
                .map(s -> s.getWorkflowState().getStateId())
                .collect(Collectors.toSet());

        return attributeDefs.stream()
                .map(name -> DataObjectDef.create(Object.class, name))
                .collect(Collectors.toList());
    }

    @Override
    public List<StateDef> getStates() {
        return new ArrayList<>(adapterMap.values().stream()
                .map(DynamicDslWorkflowAdapter::getStateDefsForWorkflow)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(stateDef -> stateDef.getWorkflowState().getStateId(), Function.identity(), (p, q) -> p)).values());
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return adapterMap.values().stream()
                .map(DynamicDslWorkflowAdapter::getSignalChannelDefForWorkflow)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
