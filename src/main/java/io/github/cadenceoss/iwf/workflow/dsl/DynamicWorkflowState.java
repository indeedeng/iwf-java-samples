package io.github.cadenceoss.iwf.workflow.dsl;

import io.github.cadenceoss.iwf.core.Context;
import io.github.cadenceoss.iwf.core.StateDecision;
import io.github.cadenceoss.iwf.core.StateMovement;
import io.github.cadenceoss.iwf.core.WorkflowState;
import io.github.cadenceoss.iwf.core.command.CommandRequest;
import io.github.cadenceoss.iwf.core.command.CommandResults;
import io.github.cadenceoss.iwf.core.communication.Communication;
import io.github.cadenceoss.iwf.core.communication.ImmutableSignalCommand;
import io.github.cadenceoss.iwf.core.communication.SignalCommand;
import io.github.cadenceoss.iwf.core.persistence.Persistence;
import io.github.cadenceoss.iwf.workflow.dsl.utils.JQFilter;
import io.serverlessworkflow.api.events.OnEvents;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.states.EventState;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.states.SwitchState;
import io.serverlessworkflow.api.switchconditions.DataCondition;
import io.serverlessworkflow.api.transitions.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicWorkflowState implements WorkflowState<State> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicWorkflowState.class);
    private final State workflowState;
    private final String workflowId;
    private final Map<String, State> stateMap;

    private DynamicWorkflowState(final String workflowId,
                                 final State workflowState,
                                 final Map<String, State> stateMap) {
        this.workflowState = workflowState;
        this.workflowId = workflowId;
        this.stateMap = stateMap;
    }

    //TODO: I don't recommend passing all states to each state wf, but it's hackweek, and I'm out of ideas.
    public static DynamicWorkflowState of(final String workflowId,
                                          final State workflowState,
                                          final Map<String, State> stateMap) {
        return new DynamicWorkflowState(workflowId, workflowState, stateMap);
    }

    @Override
    public String getStateId() {
        return this.workflowId + "-" + workflowState.getName();
    }

    @Override
    public Class<State> getInputType() {
        return State.class;
    }

    @Override
    public CommandRequest start(final Context context,
                                final State input,
                                Persistence persistence,
                                final Communication communication) {
        LOGGER.info("Received start request for input {} ", input.getName());
        if (input instanceof EventState) {
            return CommandRequest.forAllCommandCompleted(getSignalCommandsForEventState((EventState) input));
        }
        return CommandRequest.empty;
    }

    @Override
    public StateDecision decide(final Context context,
                                final State input,
                                final CommandResults commandResults,
                                Persistence persistence,
                                final Communication communication) {
        LOGGER.info("Received decide request for input {} ", input.getName());

        if (input instanceof OperationState && input.getEnd().isTerminate()) {
            return StateDecision.gracefulCompleteWorkflow();
        } else if (input instanceof SwitchState) {
            SwitchState switchState = (SwitchState) input;
            if (switchState.getDataConditions() != null && switchState.getDataConditions().size() > 0) {
                // evaluate each condition to see if it's true. If none are true default to defaultCondition
                StateMovement[] stateMovements = getStateMovements(switchState, persistence.getDataObject(workflowId + "-" + input.getName(), Object.class)).toArray(new StateMovement[]{});
                StateDecision.multiNextStates(stateMovements);
            }
        }

        Transition transition = input.getTransition();
        //If the transition state is equal to null, we've reached the end of either a branch or the wf as a whole.
        if (transition == null || transition.getNextState() == null) {
            return StateDecision.DEAD_END;
        } else {
            if (commandResults.getAllSignalCommandResults().size() > 0) {
                LOGGER.info("Registering query attributes..");
                persistence.setDataObject(workflowId + "-" + transition.getNextState(), commandResults.getAllSignalCommandResults().get(0).getSignalValue());
            }
            return StateDecision.singleNextState(workflowId + "-" + transition.getNextState(), stateMap.get(transition.getNextState()));
        }
    }

    private SignalCommand[] getSignalCommandsForEventState(final EventState input) {
        List<String> onEventNames = input.getOnEvents().stream()
                .map(OnEvents::getEventRefs)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return onEventNames.stream()
                .map(oet ->
                        ImmutableSignalCommand.builder()
                                .commandId(this.workflowId + "-" + oet)
                                .signalChannelName(this.workflowId + "-" + oet)
                                .build())
                .toArray(SignalCommand[]::new);
    }

    private List<StateMovement> getStateMovements(final SwitchState switchState, final Object signal) {
        List<StateMovement> movements = new ArrayList<>();
        for (DataCondition dataCondition : switchState.getDataConditions()) {
            if (JQFilter.getInstance()
                    .evaluateBooleanExpression(dataCondition.getCondition(), signal)) {
                Transition transition = dataCondition.getTransition();
                if (transition != null
                        && transition.getNextState() != null) {
                    StateMovement stateMovement = StateMovement.create(workflowId + "-" + transition.getNextState(), stateMap.get(transition.getNextState()));
                    movements.add(stateMovement);
                }
            }
        }
        return movements;
    }
}
