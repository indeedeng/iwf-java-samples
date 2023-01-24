package io.iworkflow.workflow.subscription;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.workflow.subscription.model.Customer;

import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyCustomer;

public class InitState implements WorkflowState<Customer> {

    @Override
    public Class<Customer> getInputType() {
        return Customer.class;
    }

    @Override
    public CommandRequest start(final Context context, final Customer input, Persistence persistence, final Communication communication) {
        persistence.setDataObject(keyCustomer, input);
        return CommandRequest.empty;
    }

    @Override
    public StateDecision decide(final Context context, final Customer input, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        return StateDecision.multiNextStates(TrialState.class, CancelState.class, UpdateBillingPeriodChargeAmountLoopState.class);
    }
}