package io.iworkflow.workflow.subscription;

import io.iworkflow.core.StateDef;
import io.iworkflow.core.Workflow;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.SignalChannelDef;
import io.iworkflow.core.persistence.DataObjectDef;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.workflow.subscription.model.Customer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

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
                StateDef.nonStartingState(new ChargeLoopState()),
                StateDef.nonStartingState(new TrialState()),
                StateDef.nonStartingState(new UpdateBillingPeriodChargeAmountLoopState())

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
