package io.iworkflow.patterns.workflow.waitforstatecompletion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.iworkflow.patterns.services.ServiceDependency;
import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

public class UpdateExternalSystemState implements WorkflowState<JobSeekerData> {
    final ServiceDependency serviceDependency;
    final ObjectMapper objectMapper;

    public UpdateExternalSystemState(final ServiceDependency serviceDependency, final ObjectMapper objectMapper) {
        this.serviceDependency = serviceDependency;
        this.objectMapper = objectMapper;
    }

    @Override
    public Class<JobSeekerData> getInputType() {
        return JobSeekerData.class;
    }

    @Override
    public StateDecision execute(final Context context, final JobSeekerData input, final CommandResults commandResults, final Persistence persistence, final Communication communication) {
        try {
            serviceDependency.updateExternalSystem(objectMapper.writeValueAsString(input));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return StateDecision.gracefulCompleteWorkflow();
    }
}
