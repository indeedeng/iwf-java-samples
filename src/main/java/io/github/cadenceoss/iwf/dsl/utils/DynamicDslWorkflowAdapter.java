package io.github.cadenceoss.iwf.dsl.utils;

import io.github.cadenceoss.iwf.core.ImmutableStateDef;
import io.github.cadenceoss.iwf.core.StateDef;
import io.github.cadenceoss.iwf.core.communication.ImmutableSignalChannelDef;
import io.github.cadenceoss.iwf.core.communication.SignalChannelDef;
import io.github.cadenceoss.iwf.dsl.DynamicWorkflowState;
import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.events.EventDefinition;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.utils.WorkflowUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DynamicDslWorkflowAdapter {
    private static final int DEFAULT_WORKFLOW_START_POS = 0;

    private final Workflow workflow;
    private final Map<String, State> stateMap;

    public DynamicDslWorkflowAdapter(final Workflow workflow) {
        this.workflow = workflow;
        stateMap = workflow.getStates().stream()
                .collect(Collectors.toMap(State::getName, Function.identity()));
    }

    public List<StateDef> getStateDefsForWorkflow() {
        List<State> states = workflow.getStates();
        List<StateDef> stateDefList = new ArrayList<>();
        for (int i = 0; i < states.size(); i++) {
            //0 is default workflow starting position.
            Optional<StateDef> stateDef = createStateDef(states.get(i), i == DEFAULT_WORKFLOW_START_POS);
            stateDef.map(stateDefList::add);
        }
        return stateDefList;
    }

    public List<SignalChannelDef> getSignalChannelDefForWorkflow() {
        if (workflow.getEvents() == null) {
            return Collections.emptyList();
        }
        return workflow.getEvents().getEventDefs().stream()
                .map(eventDefinition ->
                        ImmutableSignalChannelDef.builder()
                                .signalChannelName(getSignalChannelName(eventDefinition))
                                .signalValueType(getClassType(eventDefinition))
                                .build()
                )
                .collect(Collectors.toList());
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public State getFirstState() {
        return WorkflowUtils.getStartingState(workflow);
    }

    private Optional<StateDef> createStateDef(final State s, boolean isStart) {
        return Optional.of(ImmutableStateDef.builder()
                .canStartWorkflow(isStart)
                .workflowState(DynamicWorkflowState.of(workflow.getId(), s, stateMap))
                .build());
    }
    private String getSignalChannelName(final EventDefinition eventDefinition) {
        return workflow.getId() + "-" + eventDefinition.getName();
    }
    private Class<?> getClassType(final EventDefinition eventDefinition) {
        try {
            return Class.forName(eventDefinition.getType());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
