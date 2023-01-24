package io.iworkflow.workflow.subscription;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.workflow.subscription.model.Customer;
import io.iworkflow.workflow.subscription.model.ImmutableCustomer;
import io.iworkflow.workflow.subscription.model.ImmutableSubscription;
import io.iworkflow.workflow.subscription.model.Subscription;

import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyCustomer;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.signalUpdateBillingPeriodCharge;

public class UpdateBillingPeriodChargeAmountLoopState implements WorkflowState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest start(final Context context, final Void input, Persistence persistence, final Communication communication) {
        return CommandRequest.forAllCommandCompleted(
                SignalCommand.create(signalUpdateBillingPeriodCharge)
        );
    }

    @Override
    public StateDecision decide(final Context context, final Void input, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        final Customer customer = persistence.getDataObject(keyCustomer, Customer.class);

        final int newAmount = commandResults.getSignalValueByIndex(0);

        final Subscription updatedSubscription = ImmutableSubscription.builder().from(customer.getSubscription()).billingPeriodCharge(newAmount).build();
        final Customer updatedCustomer = ImmutableCustomer.copyOf(customer)
                .withSubscription(updatedSubscription);
        persistence.setDataObject(keyCustomer, updatedCustomer);

        return StateDecision.singleNextState(UpdateBillingPeriodChargeAmountLoopState.class);
    }
}