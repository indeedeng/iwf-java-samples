package io.github.cadenceoss.iwf.dsl;

import io.github.cadenceoss.iwf.core.Context;
import io.github.cadenceoss.iwf.core.StateDecision;
import io.github.cadenceoss.iwf.core.WorkflowState;
import io.github.cadenceoss.iwf.core.attributes.QueryAttributesRW;
import io.github.cadenceoss.iwf.core.attributes.SearchAttributesRW;
import io.github.cadenceoss.iwf.core.attributes.StateLocal;
import io.github.cadenceoss.iwf.core.command.CommandRequest;
import io.github.cadenceoss.iwf.core.command.CommandResults;
import io.github.cadenceoss.iwf.core.command.ImmutableSignalCommand;
import io.github.cadenceoss.iwf.core.command.InterStateChannel;
import io.github.cadenceoss.iwf.core.command.SignalCommand;
import io.serverlessworkflow.api.events.OnEvents;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.states.EventState;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.transitions.Transition;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DynamicWorkflowState implements WorkflowState<State> {
    private final State initialWorkflowState;
    private final String workflowId;

    private DynamicWorkflowState(final String workflowId, final State workflowSteps) {
        this.initialWorkflowState = workflowSteps;
        this.workflowId = workflowId;
    }

    public static DynamicWorkflowState of(final String workflowId,
                                          final State initialWorkflowState) {
        return new DynamicWorkflowState(workflowId, initialWorkflowState);
    }

    @Override
    public String getStateId() {
        return this.workflowId + "-" + this.initialWorkflowState.getName();
    }

    @Override
    public Class<State> getInputType() {
        return State.class;
    }

    @Override
    public CommandRequest start(final Context context,
                                final State input,
                                final StateLocal stateLocals,
                                final SearchAttributesRW searchAttributes,
                                final QueryAttributesRW queryAttributes,
                                final InterStateChannel interStateChannel) {
        if (input instanceof EventState) {
            return CommandRequest.forAllCommandCompleted(getSignalCommandsForEventState((EventState) input));
        }
        return CommandRequest.empty;
    }

    @Override
    public StateDecision decide(final Context context,
                                final State input,
                                final CommandResults commandResults,
                                final StateLocal stateLocals,
                                final SearchAttributesRW searchAttributes,
                                final QueryAttributesRW queryAttributes,
                                final InterStateChannel interStateChannel) {

        if (input instanceof OperationState && input.getEnd().isTerminate()) {
            return StateDecision.gracefulCompleteWorkflow();
        }
        Transition transition = input.getTransition();
        //If the transition state is equal to null, we've reached the end of either a branch or the wf as a whole.
        if (transition == null || transition.getNextState() == null) {
            return StateDecision.DEAD_END;
        } else {
            return StateDecision.singleNextState(workflowId + "-" + transition.getNextState(), input);
        }
    }

    private SignalCommand[] getSignalCommandsForEventState(final EventState input) {
        List<String> onEventTypes = input.getOnEvents().stream()
                .map(OnEvents::getEventRefs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return onEventTypes.stream()
                .map(oet ->
                        ImmutableSignalCommand.builder()
                                .commandId(this.workflowId + "-" + oet)
                                .signalChannelName(this.workflowId + "-" + oet)
                                .build())
                .toArray(SignalCommand[]::new);
    }
}
