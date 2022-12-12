package io.github.cadenceoss.iwf.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableStartWorkflowResponse.class)
public abstract class StartWorkflowResponse {

    public abstract String getWorkflowId();
    public abstract String getRunId();
}
