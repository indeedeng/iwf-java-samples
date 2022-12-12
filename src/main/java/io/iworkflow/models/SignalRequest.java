package io.iworkflow.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableSignalRequest.class)
public abstract class SignalRequest {
    public abstract String getSignalName();
    public abstract Object getSignal();
}
