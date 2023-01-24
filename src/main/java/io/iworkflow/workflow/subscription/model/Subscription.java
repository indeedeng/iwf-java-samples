package io.iworkflow.workflow.subscription.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.Duration;

@Value.Immutable
@JsonDeserialize(as = ImmutableSubscription.class)
public abstract class Subscription {
    public abstract Duration getTrialPeriod();

    public abstract Duration getBillingPeriod();

    public abstract int getMaxBillingPeriods();

    public abstract int getBillingPeriodCharge();
}
