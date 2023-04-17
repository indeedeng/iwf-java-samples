package io.iworkflow.workflow.engagement;

import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.StateMovement;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.command.TimerCommand;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.SignalChannelDef;
import io.iworkflow.core.communication.SignalCommand;
import io.iworkflow.core.communication.SignalCommandResult;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.core.persistence.SearchAttributeDef;
import io.iworkflow.gen.models.ChannelRequestStatus;
import io.iworkflow.gen.models.RetryPolicy;
import io.iworkflow.gen.models.SearchAttributeValueType;
import io.iworkflow.gen.models.WorkflowStateOptions;
import io.iworkflow.workflow.MyDependencyService;
import io.iworkflow.workflow.engagement.model.EngagementDescription;
import io.iworkflow.workflow.engagement.model.EngagementInput;
import io.iworkflow.workflow.engagement.model.ImmutableEngagementDescription;
import io.iworkflow.workflow.engagement.model.Status;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static io.iworkflow.workflow.engagement.EngagementWorkflow.DA_KEY_NOTES;
import static io.iworkflow.workflow.engagement.EngagementWorkflow.SA_KEY_LAST_UPDATE_TIMESTAMP;
import static io.iworkflow.workflow.engagement.EngagementWorkflow.SA_KEY_PROPOSE_USER_ID;
import static io.iworkflow.workflow.engagement.EngagementWorkflow.SA_KEY_STATUS;
import static io.iworkflow.workflow.engagement.EngagementWorkflow.SA_KEY_TARGET_USER_ID;

@Component
public class EngagementWorkflow implements ObjectWorkflow {

    private MyDependencyService myService;

    // myService will be injected by Spring
    public EngagementWorkflow(MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(
                StateDef.startingState(new InitState()),
                StateDef.nonStartingState(new ReminderState(myService)),
                StateDef.nonStartingState(new NotifyExternalSystemState(myService))
        );
    }

    public static final String SA_KEY_PROPOSE_USER_ID = "ProposeUserId";
    public static final String SA_KEY_TARGET_USER_ID = "TargetUserId";
    public static final String SA_KEY_STATUS = "Status";
    public static final String SA_KEY_LAST_UPDATE_TIMESTAMP = "LastUpdateTimeMillis";

    public static final String DA_KEY_NOTES = "Notes";

    public static final String SIGNAL_NAME_OPT_OUT_REMINDER = "OptOutReminder";

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
                SearchAttributeDef.create(SearchAttributeValueType.KEYWORD, SA_KEY_PROPOSE_USER_ID),
                SearchAttributeDef.create(SearchAttributeValueType.KEYWORD, SA_KEY_TARGET_USER_ID),
                SearchAttributeDef.create(SearchAttributeValueType.KEYWORD, SA_KEY_STATUS),
                SearchAttributeDef.create(SearchAttributeValueType.INT, SA_KEY_LAST_UPDATE_TIMESTAMP),

                DataAttributeDef.create(String.class, DA_KEY_NOTES)
        );
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return Arrays.asList(
                SignalChannelDef.create(Void.class, SIGNAL_NAME_OPT_OUT_REMINDER)
        );
    }

    @RPC
    public void decline(Context context, String notes, Persistence persistence, Communication communication) {
        final String currentStatus = persistence.getSearchAttributeKeyword(SA_KEY_STATUS);
        if (!currentStatus.equals(Status.INITIATED.name())) {
            throw new IllegalArgumentException("can only decline in INITIATED status, current is " + currentStatus);
        }

        persistence.setSearchAttributeKeyword(SA_KEY_STATUS, Status.DECLINED.name());
        persistence.setSearchAttributeInt64(SA_KEY_LAST_UPDATE_TIMESTAMP, System.currentTimeMillis());
        communication.triggerStateMovements(
                StateMovement.create(NotifyExternalSystemState.class, Status.DECLINED)
        );

        String currentNotes = persistence.getDataAttribute(DA_KEY_NOTES, String.class);
        persistence.setDataAttribute(DA_KEY_NOTES, currentNotes + ";" + notes);
    }

    @RPC
    public void accept(Context context, String notes, Persistence persistence, Communication communication) {
        final String currentStatus = persistence.getSearchAttributeKeyword(SA_KEY_STATUS);
        if (!currentStatus.equals(Status.INITIATED.name()) && !currentStatus.equals(Status.DECLINED.name())) {
            throw new IllegalArgumentException("can only accept in INITIATED or DECLINED status, current is " + currentStatus);
        }

        persistence.setSearchAttributeKeyword(SA_KEY_STATUS, Status.ACCEPTED.name());
        persistence.setSearchAttributeInt64(SA_KEY_LAST_UPDATE_TIMESTAMP, System.currentTimeMillis());
        communication.triggerStateMovements(
                StateMovement.create(NotifyExternalSystemState.class, Status.DECLINED)
        );

        String currentNotes = persistence.getDataAttribute(DA_KEY_NOTES, String.class);
        persistence.setDataAttribute(DA_KEY_NOTES, currentNotes + ";" + notes);
    }

    @RPC
    public EngagementDescription describe(Context context, Persistence persistence, Communication communication) {
        final String currentStatus = persistence.getSearchAttributeKeyword(SA_KEY_STATUS);
        String currentNotes = persistence.getDataAttribute(DA_KEY_NOTES, String.class);
        final String proposeUserId = persistence.getSearchAttributeKeyword(SA_KEY_PROPOSE_USER_ID);
        final String targetUserId = persistence.getSearchAttributeKeyword(SA_KEY_TARGET_USER_ID);
        return ImmutableEngagementDescription.builder()
                .currentStatus(Status.valueOf(currentStatus))
                .notes(currentNotes)
                .proposeUserId(proposeUserId)
                .targetUserId(targetUserId)
                .build();
    }
}

class InitState implements WorkflowState<EngagementInput> {

    @Override
    public Class<EngagementInput> getInputType() {
        return EngagementInput.class;
    }

    @Override
    public StateDecision execute(final Context context, final EngagementInput input, final CommandResults commandResults, final Persistence persistence, final Communication communication) {
        persistence.setSearchAttributeKeyword(SA_KEY_PROPOSE_USER_ID, input.getProposeUserId());
        persistence.setSearchAttributeKeyword(SA_KEY_TARGET_USER_ID, input.getTargetUserId());
        persistence.setSearchAttributeKeyword(SA_KEY_STATUS, Status.INITIATED.name());
        persistence.setSearchAttributeInt64(SA_KEY_LAST_UPDATE_TIMESTAMP, System.currentTimeMillis());

        persistence.setDataAttribute(DA_KEY_NOTES, input.getNotes());
        return StateDecision.multiNextStates(
                StateMovement.create(ReminderState.class),
                StateMovement.create(NotifyExternalSystemState.class, Status.INITIATED)
        );
    }
}

class ReminderState implements WorkflowState<Void> {

    private MyDependencyService myService;

    ReminderState(MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(final Context context, final Void input, final Persistence persistence, final Communication communication) {
        return CommandRequest.forAnyCommandCompleted(
                TimerCommand.createByDuration(Duration.ofSeconds(24)), // use 24 seconds to simulate 24 hours
                SignalCommand.create(EngagementWorkflow.SIGNAL_NAME_OPT_OUT_REMINDER) // user can choose to opt out
        );
    }

    @Override
    public StateDecision execute(final Context context, final Void input, final CommandResults commandResults, final Persistence persistence, final Communication communication) {
        final String currentStatus = persistence.getSearchAttributeKeyword(SA_KEY_STATUS);
        if (!currentStatus.equals(Status.INITIATED.name())) {
            return StateDecision.gracefulCompleteWorkflow("done");
        }

        final SignalCommandResult optOutSignalResult = commandResults.getAllSignalCommandResults().get(0);
        if (optOutSignalResult.getSignalRequestStatusEnum() == ChannelRequestStatus.RECEIVED) {
            return StateDecision.gracefulCompleteWorkflow("opt-out email");
        }
        final String targetUserId = persistence.getSearchAttributeKeyword(SA_KEY_TARGET_USER_ID);
        this.myService.sendEmail(targetUserId, "Reminder:xxx please respond", "Hello xxx, ...");

        // go back to the loop
        return StateDecision.singleNextState(ReminderState.class);
    }
}

class NotifyExternalSystemState implements WorkflowState<Status> {

    private MyDependencyService myService;

    NotifyExternalSystemState(MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public Class<Status> getInputType() {
        return Status.class;
    }

    @Override
    public StateDecision execute(final Context context, final Status status, final CommandResults commandResults, final Persistence persistence, final Communication communication) {
        final String proposeUserId = persistence.getSearchAttributeKeyword(SA_KEY_PROPOSE_USER_ID);
        final String targetUserId = persistence.getSearchAttributeKeyword(SA_KEY_TARGET_USER_ID);
        // Note that this API will fail for a few times until success
        this.myService.notifyExternalSystem("engagement from prosing user " + proposeUserId + " to target user " + targetUserId + " is now in status: " + status.name());
        return StateDecision.DEAD_END;
    }

    /**
     * By default, all state execution will retry infinitely (until workflow timeout).
     * This may not work for some dependency as we may want to retry for only a certain times
     */
    @Override
    public WorkflowStateOptions getStateOptions() {
        return new WorkflowStateOptions()
                .executeApiRetryPolicy(
                        new RetryPolicy()
                                .backoffCoefficient(2f)
                                .maximumAttempts(100)
                                .maximumAttemptsDurationSeconds(3600)
                                .initialIntervalSeconds(3)
                                .maximumIntervalSeconds(60)
                );
    }
}