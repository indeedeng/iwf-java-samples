package io.iworkflow.workflow.subscription;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateMovement;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.command.TimerCommand;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.workflow.subscription.model.Customer;

import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyBillingPeriodNum;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyCustomer;

public class ChargeLoopState implements WorkflowState<Void> {

    private final String subscriptionOverKey = "subscriptionOver";

    @Override

    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest start(final Context context, final Void input, Persistence persistence, final Communication communication) {
        final Customer customer = persistence.getDataObject(keyCustomer, Customer.class);
        final int periodNum = persistence.getDataObject(keyBillingPeriodNum, Integer.class);

        if (periodNum >= customer.getSubscription().getMaxBillingPeriods()) {
            persistence.setStateLocal(subscriptionOverKey, true);
            return CommandRequest.empty;
        }

        persistence.setDataObject(keyBillingPeriodNum, periodNum + 1);
        return CommandRequest.forAllCommandCompleted(
                TimerCommand.createByDuration(customer.getSubscription().getBillingPeriod())
        );
    }

    @Override
    public StateDecision decide(final Context context, final Void input, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        final Customer customer = persistence.getDataObject(keyCustomer, Customer.class);

        final Boolean subscriptionOver = persistence.getStateLocal(subscriptionOverKey, Boolean.class);
        if (subscriptionOver != null && subscriptionOver) {
            System.out.println("sending an subscription over email to " + customer.getEmail());
            return StateDecision.builder()
                    .addNextStates(StateMovement.forceCompleteWorkflow())
                    .build(); // TODO change to StateDecision.forceCompleteWorkflow();
        }

        System.out.printf("this is an RPC call to charge user %s for %d \n", customer.getEmail(), customer.getSubscription().getBillingPeriodCharge());

        return StateDecision.singleNextState(ChargeLoopState.class);
    }
}