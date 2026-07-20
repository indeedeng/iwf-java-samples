package io.iworkflow.patterns.workflow.recovery;

import static io.iworkflow.patterns.workflow.recovery.FailureRecoveryWorkflow.WORKFLOW_INPUT_KEY;
import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.WorkflowStateOptions;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.gen.models.RetryPolicy;

import java.util.List;
import java.util.Random;

public class OrderFlow implements Flow {
    public static final Attribute ATTR_ORDER_STATUS = Attribute.define("status", String.class)

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return List.of(ATTR_ORDER_STATUS);
    }
    ...
}
...
class PlaceOrderStep implements Step<Order> {    
    @Override
    public StepDecision execute(Context context, Order order)) {
        String currentStatus = ATTR_ORDER_STATUS.get(context);
        if ...{...}
        ATTR_ORDER_STATUS.set(context, "order_placed");
        return StepDecision.goto(CollectPaymentStep, order)
    }
}



public class FailureRecoveryWorkflow implements ObjectWorkflow {
    public static final String WORKFLOW_INPUT_KEY = "workflow-input-data-attribute-key";

    private final List<StateDef> stateDefs;

    public FailureRecoveryWorkflow() {
        final DatabaseConnection db = new DatabaseConnection();
        final PaymentProcessor paymentProcessor = new PaymentProcessor();

        this.stateDefs = List.of(
                StateDef.startingState(new UpdateItemQuantityState(db)),
                StateDef.nonStartingState(new ChargeForItemsState(db, paymentProcessor)),
                StateDef.nonStartingState(new UpdateQuantityRecoveryState(db)),
                StateDef.nonStartingState(new VoidPaymentRecoveryState(db, paymentProcessor)));
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return stateDefs;
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return List.of(
                DataAttributeDef.create(FailureRecoveryWorkflowInput.class, WORKFLOW_INPUT_KEY));
    }
}




class FanoutStep implements Step<Integer> {    
    @Override
    public StepDecision execute( Context context, int concurrency)) {
        
        List<StepMovement> steps = new ArrayList<>();
        for (int i = 0; i < CONCURRENCY; i++) {
            steps.add(StepMovement.create(ParallelThreadStartStep.class, data.get(i)));
        }
        // Start all the concurrent steps as durable "multi-threading"
        return StateDecision.gotoMulti(steps);
    }
}

...

class ParallelThreadCompletionStep implements Step<Output> {    
    @Override
    public StepDecision execute( Context context, int concurrency)) {
        
       CompletionChannel.publish(context, CompletionChannel, Output.data)
       // just complete the thread 
       return StateDecision.deadEnd()
    }
}

class FaninStep implements Step<Integer> {
    @Override
    public Condition waitFor(Context context, int concurrency){
        return Condition.AllOf(CompletionChannel.of(concurrency))
    }
    
    @Override
    public StepDecision execute( Context context, int concurrency)) {
        List<String> outputs = ConditionResults.get(context, CompletionChannel)
        ...
    }
}

class PlaceOrderStep implements Step<Order> {    
    @Override
    public StepDecision execute( Context context, Order order)) {
        ...
        if(success){
            return StepDecision.goto(CollectPaymentStep, order);
        }else{
            return StepDecision.goto(StartOverStep, order)
        }
    }
}

class ChargeForItemsState implements WorkflowState<Integer> {
    private final DatabaseConnection databaseConnection;
    private final PaymentProcessor paymentProcessor;

    ChargeForItemsState(DatabaseConnection databaseConnection, PaymentProcessor paymentProcessor) {
        this.databaseConnection = databaseConnection;
        this.paymentProcessor = paymentProcessor;
    }

    @Override
    public Class<Integer> getInputType() {
        return Integer.class;
    }

    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptions()
                .setProceedToStateWhenExecuteRetryExhausted(VoidPaymentRecoveryState.class)
                .setExecuteApiRetryPolicy(new RetryPolicy()
                        .maximumAttempts(5));
    }

    @Override
    public StateDecision execute(
            Context context,
            Integer quantityRequested,
            CommandResults commandResults,
            Persistence persistence,
            Communication communication) {
        final FailureRecoveryWorkflowInput workflowInput = persistence.getDataAttribute(
                FailureRecoveryWorkflow.WORKFLOW_INPUT_KEY, FailureRecoveryWorkflowInput.class);

        final double itemValue = databaseConnection.getItemPrice(workflowInput.getItemName());
        final double orderValue = workflowInput.getRequestedQuantity() * itemValue;
        paymentProcessor.processPayment(orderValue);

        return StateDecision.gracefulCompleteWorkflow();
    }
}

class UpdateQuantityRecoveryState implements WorkflowState<FailureRecoveryWorkflowInput> {
    private final DatabaseConnection database;

    UpdateQuantityRecoveryState(DatabaseConnection database) {
        this.database = database;
    }

    @Override
    public Class<FailureRecoveryWorkflowInput> getInputType() {
        return FailureRecoveryWorkflowInput.class;
    }

    @Override
    public StateDecision execute(
            Context context,
            FailureRecoveryWorkflowInput input,
            CommandResults commandResults,
            Persistence persistence,
            Communication communication) {
        database.increaseQuantity(input.getItemName(), input.getRequestedQuantity());

        return StateDecision.forceFailWorkflow("Failed to process transaction");
    }
}

class VoidPaymentRecoveryState implements WorkflowState<Integer> {
    private final DatabaseConnection database;
    private final PaymentProcessor paymentProcessor;

    VoidPaymentRecoveryState(DatabaseConnection database, PaymentProcessor paymentProcessor) {
        this.database = database;
        this.paymentProcessor = paymentProcessor;
    }

    @Override
    public Class<Integer> getInputType() {
        return Integer.class;
    }

    @Override
    public StateDecision execute(Context context, Integer input, CommandResults commandResults, Persistence persistence, Communication communication) {
        final FailureRecoveryWorkflowInput workflowInput = persistence.getDataAttribute(
                FailureRecoveryWorkflow.WORKFLOW_INPUT_KEY, FailureRecoveryWorkflowInput.class);

        final double itemValue = database.getItemPrice(workflowInput.getItemName());
        final double orderValue = workflowInput.getRequestedQuantity() * itemValue;
        paymentProcessor.voidPayment(orderValue);

        return StateDecision.singleNextState(UpdateQuantityRecoveryState.class, workflowInput);
    }
}

class DatabaseConnection {
    private static final Random RANDOM = new Random();

    public void reduceQuantity(final String itemName, final int quantity) {
        System.out.println("Reducing quantity: " + quantity);
        if (quantity > RANDOM.nextInt(10)) {
            throw new RuntimeException("not enough items available");
        }
    }

    public void increaseQuantity(final String itemName, final int quantity) {
        System.out.println("Increasing quantity: " + quantity);
    }

    public double getItemPrice(final String itemName) {
        return 3.14;
    }
}

class PaymentProcessor {
    public void processPayment(final double price) {
        throw new RuntimeException("Payment could not be processed");
    }

    public void voidPayment(final double price) {
        System.out.printf("Voiding payment for $ %.2f%n", price);
    }
}
