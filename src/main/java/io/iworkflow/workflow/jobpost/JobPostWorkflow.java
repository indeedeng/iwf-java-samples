package io.iworkflow.workflow.jobpost;

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
import io.iworkflow.core.persistence.SearchAttributeDef;
import io.iworkflow.gen.models.SearchAttributeValueType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class JobPostWorkflow implements ObjectWorkflow {

    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(
                StateDef.nonStartingState(new ExternalUpdateState()),
                StateDef.nonStartingState(new ClosingState())
        );
    }

    public static final String SA_KEY_JOB_DESCRIPTION = "JobDescription";
    public static final String SA_KEY_TITLE = "Title";
    public static final String SA_KEY_LAST_UPDATE_TIMESTAMP = "LastUpdateTimeMillis";

    public static final String DA_KEY_NOTES = "Notes";

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(

                SearchAttributeDef.create(SearchAttributeValueType.TEXT, SA_KEY_JOB_DESCRIPTION),
                SearchAttributeDef.create(SearchAttributeValueType.KEYWORD, SA_KEY_TITLE),
                SearchAttributeDef.create(SearchAttributeValueType.INT, SA_KEY_LAST_UPDATE_TIMESTAMP),

                DataAttributeDef.create(String.class, DA_KEY_NOTES)
        );
    }

    @RPC
    public void update(Context context, JobUpdateInput input, Persistence persistence, Communication communication) {
        communication.triggerStateMovements(
                StateMovement.create(ExternalUpdateState.class)
        );
        persistence.setDataAttribute(SA_KEY_TITLE, input.getTitle());
        persistence.setDataAttribute(SA_KEY_JOB_DESCRIPTION, input.getDescription());
        persistence.setDataAttribute(DA_KEY_NOTES, input.getNotes());
        persistence.setSearchAttributeInt64(SA_KEY_LAST_UPDATE_TIMESTAMP, System.currentTimeMillis());
    }

    @RPC
    public void close(Context context, Persistence persistence, Communication communication) {
        communication.triggerStateMovements(
                StateMovement.create(ClosingState.class)
        );
    }

}

class ExternalUpdateState implements WorkflowState<Void> {

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
        System.out.println("closing workflow, it will be deleted after retention");
        return StateDecision.gracefulCompleteWorkflow();
    }
}
