package io.iworkflow.patterns.workflow.waitforstatecompletion;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.iworkflow.patterns.services.ServiceDependency;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

public class PersistDataState implements WorkflowState<JobSeekerData> {
    final ServiceDependency mongoCollection;

    public PersistDataState(final ServiceDependency mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

    @Override
    public Class<JobSeekerData> getInputType() {
        return JobSeekerData.class;
    }

    @Override
    public StateDecision execute(final Context context, final JobSeekerData input, final CommandResults commandResults, final Persistence persistence, final Communication communication) {
        try {
            mongoCollection.upsert(input);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        persistence.setDataAttribute(WaitForStateCompletionWorkflow.JOB_SEEKER_DATA, input);
        return StateDecision.singleNextState(UpdateExternalSystemState.class, input);
    }
}
