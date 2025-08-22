package io.iworkflow.patterns.workflow.parallel;

import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.StateMovement;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

import java.util.List;

public class SimpleParallelStatesWorkflow implements ObjectWorkflow {
    private final List<StateDef> stateDefs;

    public SimpleParallelStatesWorkflow() {
        this.stateDefs = List.of(
                StateDef.startingState(new InitState()),
                StateDef.nonStartingState(new SendTextMessageState()),
                StateDef.nonStartingState(new SendEmailState())
        );
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return stateDefs;
    }
}


class InitState implements WorkflowState<JobSeeker> {
    @Override
    public Class<JobSeeker> getInputType() {
        return JobSeeker.class;
    }

    @Override
    public StateDecision execute(
            Context context,
            JobSeeker input,
            CommandResults commandResults,
            Persistence persistence,
            Communication communication) {
        // Run two states concurrently
        return StateDecision.multiNextStates(
                StateMovement.create(SendTextMessageState.class, input.phoneNumber()),
                StateMovement.create(SendEmailState.class, input.email())
        );
    }
}

class SendTextMessageState implements WorkflowState<String> {
    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public StateDecision execute(
            Context context,
            String input,
            CommandResults commandResults,
            Persistence persistence,
            Communication communication) {
        final String message = "[FAKE] Sending text message to: " + input;

        System.out.println(message);
        persistence.recordEvent("text-message", message);

        return StateDecision.gracefulCompleteWorkflow();
    }
}

class SendEmailState implements WorkflowState<String> {
    @Override
    public Class<String> getInputType() {
        return String.class;
    }

    @Override
    public StateDecision execute(
            Context context,
            String input,
            CommandResults commandResults,
            Persistence persistence,
            Communication communication) {
        final String message = "[FAKE] Sending email to: " + input;

        System.out.println(message);
        persistence.recordEvent("email-notification", message);

        return StateDecision.gracefulCompleteWorkflow();
    }
}