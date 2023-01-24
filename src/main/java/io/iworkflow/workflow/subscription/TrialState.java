package io.iworkflow.workflow.subscription;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.command.TimerCommand;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.workflow.subscription.model.Customer;

import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyBillingPeriodNum;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyCustomer;

public class TrialState implements WorkflowState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest start(final Context context, final Void input, Persistence persistence, final Communication communication) {
        final Customer customer = persistence.getDataObject(keyCustomer, Customer.class);
        System.out.println("sending an welcome email to " + customer.getEmail());

        return CommandRequest.forAllCommandCompleted(
                TimerCommand.createByDuration(customer.getSubscription().getTrialPeriod())
        );
    }

    @Override
    public StateDecision decide(final Context context, final Void input, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        persistence.setDataObject(keyBillingPeriodNum, 0);
        return StateDecision.singleNextState(ChargeLoopState.class);
    }
}