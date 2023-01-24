package io.iworkflow.workflow.subscription.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableCustomer.class)
public abstract class Customer {
    public abstract String getFirstName();

    public abstract String getLastName();

    public abstract String getId();

    public abstract String getEmail();

    public abstract Subscription getSubscription();
}
