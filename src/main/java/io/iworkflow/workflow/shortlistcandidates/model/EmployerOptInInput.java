package io.iworkflow.workflow.shortlistcandidates.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableEmployerOptInInput.class)
public abstract class EmployerOptInInput {
    public abstract String getEmployerId();
}
