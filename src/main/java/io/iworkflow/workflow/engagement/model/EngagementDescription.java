package io.iworkflow.workflow.engagement.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonDeserialize(as = ImmutableEngagementDescription.class)
public abstract class EngagementDescription {
    public abstract String getProposeUserId();

    public abstract String getTargetUserId();

    public abstract String getNotes();
    public abstract Status getCurrentStatus();
}
