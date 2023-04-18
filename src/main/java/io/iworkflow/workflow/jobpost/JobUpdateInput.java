package io.iworkflow.workflow.jobpost;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableJobUpdateInput.class)
public abstract class JobUpdateInput {
    public abstract String getTitle();

    public abstract String getDescription();

    public abstract String getNotes();
}
