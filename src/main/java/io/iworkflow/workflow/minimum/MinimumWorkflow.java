package io.iworkflow.workflow.minimum;

import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.StateMovement;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MinimumWorkflow implements ObjectWorkflow {

    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(
                StateDef.nonStartingState(new DeadEndState()),
                StateDef.nonStartingState(new ClosingState())
        );
    }

    public static final String DA_KEY_NOTES = "Notes";

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
                DataAttributeDef.create(String.class, DA_KEY_NOTES)
        );
    }

    @RPC
    public void executeInBackground(Context context, String notes, Persistence persistence, Communication communication) {
        communication.triggerStateMovements(
                StateMovement.create(DeadEndState.class)
        );

        String currentNotes = persistence.getDataAttribute(DA_KEY_NOTES, String.class);
        persistence.setDataAttribute(DA_KEY_NOTES, currentNotes + ";" + notes);
    }

    @RPC
    public void close(Context context, String notes, Persistence persistence, Communication communication) {
        communication.triggerStateMovements(
                StateMovement.create(ClosingState.class)
        );

        String currentNotes = persistence.getDataAttribute(DA_KEY_NOTES, String.class);
        persistence.setDataAttribute(DA_KEY_NOTES, currentNotes + ";" + notes);
    }

}

class DeadEndState implements WorkflowState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public StateDecision execute(final Context context, final Void input, final CommandResults commandResults, final Persistence persistence, final Communication communication) {
        System.out.println("in executeInBackground");
        return StateDecision.deadEnd();
    }
}

class ClosingState implements WorkflowState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public StateDecision execute(final Context context, final Void input, final CommandResults commandResults, final Persistence persistence, final Communication communication) {
        System.out.println("closing workflow");
        return StateDecision.gracefulCompleteWorkflow();
    }
}
