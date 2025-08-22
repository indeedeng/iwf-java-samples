package io.iworkflow.patterns.workflow.interruptible;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableWorkJobParametersInput.class)
public abstract class WorkJobParametersInput {
    public abstract int getJobUpperBound();

    // Optional field
    @Value.Default
    public int getProgress() {
        return 1;
    }
}