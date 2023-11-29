package io.iworkflow.workflow.money.transfer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.iworkflow.workflow.jobpost.ImmutableJobInfo;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonDeserialize(as = ImmutableTransferRequest.class)
public abstract class TransferRequest {
    public abstract String getFromAccountId();

    public abstract String getToAccountId();

    public abstract int getAmount();

    public abstract Optional<String> getNotes();
}
