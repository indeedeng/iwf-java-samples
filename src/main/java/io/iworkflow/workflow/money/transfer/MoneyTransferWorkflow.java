package io.iworkflow.workflow.money.transfer;

import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.WorkflowStateOptionsExtension;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.command.TimerCommand;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.InternalChannelCommand;
import io.iworkflow.core.communication.InternalChannelDef;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.gen.models.ChannelRequestStatus;
import io.iworkflow.gen.models.RetryPolicy;
import io.iworkflow.gen.models.WorkflowStateOptions;
import io.iworkflow.workflow.MyDependencyService;
import io.iworkflow.workflow.microservices.SignupForm;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static io.iworkflow.workflow.signup.UserSignupWorkflow.DA_FORM;
import static io.iworkflow.workflow.signup.UserSignupWorkflow.DA_Status;
import static io.iworkflow.workflow.signup.UserSignupWorkflow.VERIFY_CHANNEL;

@Component

public class MoneyTransferWorkflow implements ObjectWorkflow {
    private MyDependencyService myService;

    public MoneyTransferWorkflow(MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(
                StateDef.startingState(new VerifyState(myService)),
                StateDef.nonStartingState(new CreateDebitMemoState(myService)),
                StateDef.nonStartingState(new DebitState(myService)),
                StateDef.nonStartingState(new CreateCreditMemoState(myService)),
                StateDef.nonStartingState(new CreditState(myService)),
                StateDef.nonStartingState(new CleanupState(myService))
        );
    }
}

class VerifyState implements WorkflowState<TransferRequest> {

    private MyDependencyService myService;

    public VerifyState(final MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public Class<TransferRequest> getInputType() {
        return TransferRequest.class;
    }

    @Override
    public StateDecision execute(final Context context, final TransferRequest request, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        boolean hasSufficientFunds = myService.checkBalance(request.getFromAccountId(), request.getAmount());
        if (!hasSufficientFunds) {
            StateDecision.forceCompleteWorkflow("insufficient funds");
        }
        return StateDecision.singleNextState(CreateDebitMemoState.class, request);
    }
}

class CreateDebitMemoState implements WorkflowState<TransferRequest> {
    private MyDependencyService myService;

    public CreateDebitMemoState(final MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public Class<TransferRequest> getInputType() {
        return TransferRequest.class;
    }

    @Override
    public StateDecision execute(final Context context, final TransferRequest request, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        myService.createDebitMemo(request.getFromAccountId(), request.getAmount(), request.getNotes());
        return StateDecision.singleNextState(DebitState.class, request);
    }

    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptionsExtension()
                .setProceedOnExecuteFailure(CleanupState.class)
                .executeApiRetryPolicy(new RetryPolicy()
                        .maximumAttemptsDurationSeconds(3600));
    }
}

class DebitState implements WorkflowState<TransferRequest> {
    private MyDependencyService myService;

    public DebitState(final MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public Class<TransferRequest> getInputType() {
        return TransferRequest.class;
    }

    @Override
    public StateDecision execute(final Context context, final TransferRequest request, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        myService.debit(request.getFromAccountId(), request.getAmount());
        return StateDecision.singleNextState(CreateCreditMemoState.class, request);
    }

    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptionsExtension()
                .setProceedOnExecuteFailure(CleanupState.class)
                .executeApiRetryPolicy(new RetryPolicy()
                        .maximumAttemptsDurationSeconds(3600));
    }
}

class CreateCreditMemoState implements WorkflowState<TransferRequest> {
    private MyDependencyService myService;

    public CreateCreditMemoState(final MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public Class<TransferRequest> getInputType() {
        return TransferRequest.class;
    }

    @Override
    public StateDecision execute(final Context context, final TransferRequest request, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        myService.createCreditMemo(request.getToAccountId(), request.getAmount(), request.getNotes());
        return StateDecision.singleNextState(CreditState.class, request);
    }

    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptionsExtension()
                .setProceedOnExecuteFailure(CleanupState.class)
                .executeApiRetryPolicy(new RetryPolicy()
                        .maximumAttemptsDurationSeconds(3600));
    }
}

class CreditState implements WorkflowState<TransferRequest> {
    private MyDependencyService myService;

    public CreditState(final MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public Class<TransferRequest> getInputType() {
        return TransferRequest.class;
    }

    @Override
    public StateDecision execute(final Context context, final TransferRequest request, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        myService.credit(request.getToAccountId(), request.getAmount());
        return StateDecision.gracefulCompleteWorkflow(
                String.format("transfer is done %d from %s to %s", request.getAmount(), request.getFromAccountId(), request.getToAccountId())
        );
    }

    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptionsExtension()
                .setProceedOnExecuteFailure(CleanupState.class)
                .executeApiRetryPolicy(new RetryPolicy()
                        .maximumAttemptsDurationSeconds(3600));
    }
}

class CleanupState implements WorkflowState<TransferRequest> {
    private MyDependencyService myService;

    public CleanupState(final MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public Class<TransferRequest> getInputType() {
        return TransferRequest.class;
    }

    @Override
    public StateDecision execute(final Context context, final TransferRequest request, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        myService.undoCreateDebitMemo(request.getFromAccountId(), request.getAmount(), request.getNotes());
        myService.undoDebit(request.getFromAccountId(), request.getAmount());
        myService.undoCreateCreditMemo(request.getToAccountId(), request.getAmount(), request.getNotes());
        myService.undoCredit(request.getToAccountId(), request.getAmount());
        return StateDecision.forceFailWorkflow("failed to transfer money");
    }

    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptions()
                .executeApiRetryPolicy(new RetryPolicy()
                        .maximumAttemptsDurationSeconds(86400));
    }
}

