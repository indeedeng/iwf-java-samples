package io.iworkflow.workflow.engagement.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableEngagementInput.class)
public abstract class EngagementInput {
    public abstract String getProposeUserId();

    public abstract String getTargetUserId();

    public abstract String getNotes();
}
