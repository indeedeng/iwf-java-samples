package io.iworkflow.workflow.subscription;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.Workflow;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.command.TimerCommand;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.SignalChannelDef;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.DataObjectDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.workflow.subscription.model.Customer;
import io.iworkflow.workflow.subscription.model.ImmutableCustomer;
import io.iworkflow.workflow.subscription.model.ImmutableSubscription;
import io.iworkflow.workflow.subscription.model.Subscription;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyBillingPeriodNum;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyCustomer;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.signalCancelSubscription;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.signalUpdateBillingPeriodCharge;

@Component
public class SubscriptionWorkflow implements Workflow {

    public static final String keyBillingPeriodNum = "billingPeriodNum";
    public static final String keyCustomer = "customer";

    public static final String signalCancelSubscription = "cancelSubscription";
    public static final String signalUpdateBillingPeriodCharge = "updateBillingPeriodCharge";

    @Override
    public List<StateDef> getStates() {
        return Arrays.asList(
                StateDef.startingState(new InitState()),
                StateDef.nonStartingState(new CancelState()),
                StateDef.nonStartingState(new ChargeCurrentBillState()),
                StateDef.nonStartingState(new TrialState()),
                StateDef.nonStartingState(new UpdateChargeAmountState())

        );
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
                DataObjectDef.create(Customer.class, keyCustomer),
                DataObjectDef.create(Integer.class, keyBillingPeriodNum)
        );
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return Arrays.asList(
                SignalChannelDef.create(Void.class, signalCancelSubscription),
                SignalChannelDef.create(Integer.class, signalUpdateBillingPeriodCharge)
        );
    }
}

class InitState implements WorkflowState<Customer> {

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
        return StateDecision.multiNextStates(TrialState.class, CancelState.class, UpdateChargeAmountState.class);
    }
}

class TrialState implements WorkflowState<Void> {

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
        return StateDecision.singleNextState(ChargeCurrentBillState.class);
    }
}

class ChargeCurrentBillState implements WorkflowState<Void> {

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
            return StateDecision.forceCompleteWorkflow();
        }

        System.out.printf("this is an RPC call to charge user %s for %d \n", customer.getEmail(), customer.getSubscription().getBillingPeriodCharge());

        return StateDecision.singleNextState(ChargeCurrentBillState.class);
    }
}

class UpdateChargeAmountState implements WorkflowState<Void> {

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

        return StateDecision.singleNextState(UpdateChargeAmountState.class);
    }
}

class CancelState implements WorkflowState<Void> {

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

        return StateDecision.forceCompleteWorkflow();
    }
}