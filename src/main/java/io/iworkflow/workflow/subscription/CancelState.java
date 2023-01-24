package io.iworkflow.workflow.subscription;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateMovement;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.workflow.subscription.model.Customer;

import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyCustomer;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.signalCancelSubscription;

public class CancelState implements WorkflowState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest start(final Context context, final Void input, Persistence persistence, final Communication communication) {
        return CommandRequest.forAllCommandCompleted(
                SignalCommand.create(signalCancelSubscription)
        );
    }

    @Override
    public StateDecision decide(final Context context, final Void input, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        final Customer customer = persistence.getDataObject(keyCustomer, Customer.class);

        System.out.println("sending an cancellation email to " + customer.getEmail());

        return StateDecision.builder()
                .addNextStates(StateMovement.forceCompleteWorkflow())
                .build(); // TODO change to StateDecision.forceCompleteWorkflow();
    }
}