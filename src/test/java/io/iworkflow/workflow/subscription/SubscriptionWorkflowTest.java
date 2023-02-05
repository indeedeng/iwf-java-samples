package io.iworkflow.workflow.subscription;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.command.TimerCommand;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.workflow.subscription.model.Customer;
import io.iworkflow.workflow.subscription.model.ImmutableCustomer;
import io.iworkflow.workflow.subscription.model.ImmutableSubscription;
import io.iworkflow.workflow.subscription.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static io.iworkflow.workflow.subscription.ChargeCurrentBillState.subscriptionOverKey;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyBillingPeriodNum;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.keyCustomer;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.signalCancelSubscription;
import static io.iworkflow.workflow.subscription.SubscriptionWorkflow.signalUpdateBillingPeriodCharge;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscriptionWorkflowTest {
    @Mock
    private MyService myService;

    @Mock
    private Context context;
    @Mock
    private Persistence persistence;
    @Mock
    private Communication communication;
    @Mock
    private CommandResults commandResults;

    private Customer testCustomer = ImmutableCustomer.builder()
            .id("123")
            .email("qlong.seattle@gmail.com")
            .lastName("Long")
            .firstName("Quanzheng")
            .subscription(
                    ImmutableSubscription.builder()
                            .billingPeriodCharge(100)
                            .maxBillingPeriods(10)
                            .trialPeriod(Duration.ofSeconds(2))
                            .billingPeriod(Duration.ofSeconds(1))
                            .build()
            )
            .build();

    private InitState initState;
    private CancelState cancelState;
    private ChargeCurrentBillState chargeCurrentBillState;
    private TrialState trialState;

    private UpdateChargeAmountState updateChargeAmountState;

    @BeforeEach
    void beforeEach() {
        cancelState = new CancelState(myService);
        chargeCurrentBillState = new ChargeCurrentBillState(myService);
        trialState = new TrialState(myService);
        initState = new InitState();
        updateChargeAmountState = new UpdateChargeAmountState();
    }

    @Test
    public void testInitStateStart() {
        final CommandRequest commandRequest = initState.start(context, testCustomer, persistence, communication);

        assertEquals(CommandRequest.empty, commandRequest);
        verify(persistence).setDataObject(keyCustomer, testCustomer);
    }

    @Test
    public void testInitStateDecide() {
        final StateDecision decision = initState.decide(context, null, commandResults, persistence, communication);

        assertEquals(StateDecision.multiNextStates(TrialState.class, CancelState.class, UpdateChargeAmountState.class), decision);
    }

    @Test
    public void testTrialStateStart() {
        when(persistence.getDataObject(keyCustomer, Customer.class)).thenReturn(testCustomer);
        final CommandRequest commandRequest = trialState.start(context, null, persistence, communication);

        assertEquals(CommandRequest.forAllCommandCompleted(
                        TimerCommand.createByDuration(testCustomer.getSubscription().getTrialPeriod())
                ),
                commandRequest);
        verify(persistence).getDataObject(keyCustomer, Customer.class);
    }

    @Test
    public void testTrialStateDecide() {
        final StateDecision decision = trialState.decide(context, null, commandResults, persistence, communication);

        assertEquals(StateDecision.singleNextState(ChargeCurrentBillState.class), decision);
        verify(persistence).setDataObject(keyBillingPeriodNum, 0);
    }

    @Test
    public void testChargeCurrentBillStateStart_waitForDuration() {
        when(persistence.getDataObject(keyCustomer, Customer.class)).thenReturn(testCustomer);
        when(persistence.getDataObject(keyBillingPeriodNum, Integer.class)).thenReturn(0);

        final CommandRequest commandRequest = chargeCurrentBillState.start(context, null, persistence, communication);
        assertEquals(CommandRequest.forAllCommandCompleted(
                TimerCommand.createByDuration(testCustomer.getSubscription().getBillingPeriod())
        ), commandRequest);
        verify(persistence).getDataObject(keyCustomer, Customer.class);
        verify(persistence).getDataObject(keyBillingPeriodNum, Integer.class);
        verify(persistence).setDataObject(keyBillingPeriodNum, 1);
    }

    @Test
    public void testChargeCurrentBillStateStart_subscriptionOver() {
        when(persistence.getDataObject(keyCustomer, Customer.class)).thenReturn(testCustomer);
        when(persistence.getDataObject(keyBillingPeriodNum, Integer.class)).thenReturn(testCustomer.getSubscription().getMaxBillingPeriods());

        final CommandRequest commandRequest = chargeCurrentBillState.start(context, null, persistence, communication);
        assertEquals(CommandRequest.empty, commandRequest);

        verify(persistence).getDataObject(keyCustomer, Customer.class);
        verify(persistence).getDataObject(keyBillingPeriodNum, Integer.class);
        verify(persistence).setStateLocal(subscriptionOverKey, true);
    }

    @Test
    public void testChargeCurrentBillStateDecide_subscriptionNotOver() {
        when(persistence.getDataObject(keyCustomer, Customer.class)).thenReturn(testCustomer);
        when(persistence.getStateLocal(subscriptionOverKey, Boolean.class)).thenReturn(null);

        final StateDecision decision = chargeCurrentBillState.decide(context, null, commandResults, persistence, communication);
        assertEquals(StateDecision.singleNextState(ChargeCurrentBillState.class), decision);

        verify(persistence).getDataObject(keyCustomer, Customer.class);
        verify(persistence).getStateLocal(subscriptionOverKey, Boolean.class);
        verify(myService).chargeUser(testCustomer.getEmail(), testCustomer.getId(), testCustomer.getSubscription().getBillingPeriodCharge());
    }

    @Test
    public void testChargeCurrentBillStateDecide_subscriptionOver() {
        when(persistence.getDataObject(keyCustomer, Customer.class)).thenReturn(testCustomer);
        when(persistence.getStateLocal(subscriptionOverKey, Boolean.class)).thenReturn(true);

        final StateDecision decision = chargeCurrentBillState.decide(context, null, commandResults, persistence, communication);
        assertEquals(StateDecision.forceCompleteWorkflow(), decision);

        verify(persistence).getDataObject(keyCustomer, Customer.class);
        verify(persistence).getStateLocal(subscriptionOverKey, Boolean.class);
        verify(myService).sendEmail(any(), any(), any());
    }

    @Test
    public void testUpdateChargeAmountStateStart() {
        final CommandRequest commandRequest = updateChargeAmountState.start(context, null, persistence, communication);

        assertEquals(CommandRequest.forAllCommandCompleted(
                        SignalCommand.create(signalUpdateBillingPeriodCharge)
                ),
                commandRequest);
    }

    @Test
    public void testUpdateChargeAmountStateDecide() {
        when(persistence.getDataObject(keyCustomer, Customer.class)).thenReturn(testCustomer);
        when(commandResults.getSignalValueByIndex(0)).thenReturn(200);

        final StateDecision decision = updateChargeAmountState.decide(context, null, commandResults, persistence, communication);

        assertEquals(StateDecision.singleNextState(UpdateChargeAmountState.class), decision);

        final Subscription updatedSubscription = ImmutableSubscription.builder().from(testCustomer.getSubscription()).billingPeriodCharge(200).build();
        final Customer updatedCustomer = ImmutableCustomer.copyOf(testCustomer)
                .withSubscription(updatedSubscription);

        verify(persistence).setDataObject(keyCustomer, updatedCustomer);
        verify(persistence).getDataObject(keyCustomer, Customer.class);
        verify(commandResults).getSignalValueByIndex(0);
    }

    @Test
    public void testCancelStateStart() {
        final CommandRequest commandRequest = cancelState.start(context, null, persistence, communication);

        assertEquals(CommandRequest.forAllCommandCompleted(
                        SignalCommand.create(signalCancelSubscription)
                ),
                commandRequest);
    }

    @Test
    public void testCancelStateDecide() {
        when(persistence.getDataObject(keyCustomer, Customer.class)).thenReturn(testCustomer);
        final StateDecision decision = cancelState.decide(context, null, commandResults, persistence, communication);

        assertEquals(StateDecision.forceCompleteWorkflow(), decision);

        verify(persistence).getDataObject(keyCustomer, Customer.class);
    }
}
