package io.iworkflow.patterns.workflow.waitforstatecompletion;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iworkflow.patterns.services.ServiceDependency;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;

import java.util.Arrays;
import java.util.List;

public class WaitForStateCompletionWorkflow implements ObjectWorkflow {

    public static final String JOB_SEEKER_DATA = "job_seeker_data";
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public ServiceDependency mongoCollection;
    public ServiceDependency externalService;

    public WaitForStateCompletionWorkflow(final ServiceDependency mongoCollection, final ServiceDependency externalService) {
        this.mongoCollection = mongoCollection;
        this.externalService = externalService;
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(StateDef.startingState(new PersistDataState(mongoCollection)),
                StateDef.nonStartingState(new UpdateExternalSystemState(externalService, MAPPER)));
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
                DataAttributeDef.create(JobSeekerData.class, JOB_SEEKER_DATA)
        );
    }

    @RPC
    public JobSeekerData getJobSeekerData(final Context context, final Persistence persistence, final Communication communication) {
        final JobSeekerData data = persistence.getDataAttribute(JOB_SEEKER_DATA, JobSeekerData.class);
        if (data == null) {
            throw new IllegalStateException("Job seeker data was not persisted to the data store");
        }
        return data;
    }
}

