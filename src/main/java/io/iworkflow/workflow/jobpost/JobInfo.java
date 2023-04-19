package io.iworkflow.workflow.jobpost;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonDeserialize(as = ImmutableJobInfo.class)
public abstract class JobInfo {
    public abstract String getTitle();

    public abstract String getDescription();

    public abstract Optional<String> getNotes();
}
