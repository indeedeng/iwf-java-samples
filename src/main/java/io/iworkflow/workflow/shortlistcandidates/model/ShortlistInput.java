package io.iworkflow.workflow.shortlistcandidates.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableShortlistInput.class)
public abstract class ShortlistInput {
    public abstract String getEmployerId();
    public abstract String getCandidateId();
}
