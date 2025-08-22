package io.iworkflow.patterns.workflow.drainchannels.internal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableMongoDocument.class)
public abstract class MongoDocument {
    public abstract String getId();

    @Value.Default
    public String getStatus() {
        return "RECEIVED";
    }

    @Value.Default
    public boolean isFinalCommand() {
        return false;
    }
}
