package io.github.cadenceoss.iwf.dsl;

import io.github.cadenceoss.iwf.core.StateDef;
import io.github.cadenceoss.iwf.core.Workflow;
import io.github.cadenceoss.iwf.core.command.SignalChannelDef;
import io.github.cadenceoss.iwf.dsl.utils.DynamicDslWorkflowAdapter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicDslWorkflow implements Workflow {
    private final Map<String, DynamicDslWorkflowAdapter> adapterMap;

    public DynamicDslWorkflow(final Map<String, DynamicDslWorkflowAdapter> adapterMap) {
        this.adapterMap = adapterMap;
    }

    @Override
    public List<StateDef> getStates() {
        return adapterMap.values().stream()
                .map(DynamicDslWorkflowAdapter::getStateDefsForWorkflow)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<SignalChannelDef> getSignalChannels() {
        return adapterMap.values().stream()
                .map(DynamicDslWorkflowAdapter::getSignalChannelDefForWorkflow)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
