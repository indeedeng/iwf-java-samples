package io.iworkflow.patterns.workflow.scalableparallel;

import io.iworkflow.core.Client;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.command.TimerCommand;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.exceptions.NoRunningWorkflowException;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;

import java.time.Duration;
import java.util.List;
import java.util.Random;

/**
 * A workflow of processing a task
 */
public class ChildWorkflow implements ObjectWorkflow {

    public static final String PARENT_WORKFLOW_ID = "ParentWorkflowId";

    private final List<StateDef> stateDefs;

    public ChildWorkflow(Client iwfClient) {
        this.stateDefs = List.of(
                StateDef.startingState(new ProcessingState(iwfClient))
        );
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return List.of(
                DataAttributeDef.create(String.class, PARENT_WORKFLOW_ID)
        );
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return stateDefs;
    }

}

class ProcessingState implements WorkflowState<String> {

    private final Client iwfClient;

    public ProcessingState(Client iwfClient) {
        this.iwfClient = iwfClient;
    }

    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public CommandRequest waitUntil(final Context context, final String input, final Persistence persistence, final Communication communication) {
        final int random = new Random().nextInt(60);
        return CommandRequest.forAnyCommandCompleted(
                // Timer to simulate a long running process
                TimerCommand.createByDuration(Duration.ofSeconds(random))
        );
    }

    @Override
    public StateDecision execute(final Context context, final String input, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        // This is set by startWorkflow WorkflowOptions as initial data attribute
        // It can also be passed by startWorkflow request, but here is to demonstrate how to use initial data attribute for convenience
        final String parentWorkflowId = persistence.getDataAttribute(ChildWorkflow.PARENT_WORKFLOW_ID, String.class);

        final ParentWorkflow stub = iwfClient.newRpcStub(ParentWorkflow.class, parentWorkflowId);
        try {
            iwfClient.invokeRPC(stub::completeChildWorkflow, context.getWorkflowId());
        } catch (NoRunningWorkflowException e) {
            System.out.println("Parent workflow may have completed, might be duplicate completion request, ignore it.");
        }

        return StateDecision.gracefulCompleteWorkflow();
    }
}