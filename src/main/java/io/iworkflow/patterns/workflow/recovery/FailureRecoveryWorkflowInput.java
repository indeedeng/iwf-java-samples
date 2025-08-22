package io.iworkflow.patterns.workflow.recovery;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableFailureRecoveryWorkflowInput.class)
public abstract class FailureRecoveryWorkflowInput {
    public abstract String getItemName();
    public abstract int getRequestedQuantity();
}
