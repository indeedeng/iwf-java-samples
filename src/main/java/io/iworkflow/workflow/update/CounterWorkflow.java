package io.iworkflow.workflow.update;

import io.iworkflow.core.Context;
import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.RPC;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.communication.CommunicationMethodDef;
import io.iworkflow.core.communication.InternalChannelDef;
import io.iworkflow.core.persistence.DataAttributeDef;
import io.iworkflow.core.persistence.Persistence;
import io.iworkflow.core.persistence.PersistenceFieldDef;
import io.iworkflow.workflow.MyDependencyService;
import io.iworkflow.workflow.microservices.SignupForm;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component

public class UserSignupWorkflow implements ObjectWorkflow {

    public static final String DA_FORM = "Form";

    public static final String DA_Status = "Status";
    public static final String VERIFY_CHANNEL = "Verify";

    private MyDependencyService myService;

    public UserSignupWorkflow(MyDependencyService myService) {
        this.myService = myService;
    }

    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(
                StateDef.startingState(new SubmitState(myService)),
                StateDef.nonStartingState(new VerifyState(myService))
        );
    }

    @Override
    public List<PersistenceFieldDef> getPersistenceSchema() {
        return Arrays.asList(
                DataAttributeDef.create(SignupForm.class, DA_FORM),
                DataAttributeDef.create(String.class, DA_Status)
        );
    }

    @Override
    public List<CommunicationMethodDef> getCommunicationSchema() {
        return Arrays.asList(
                InternalChannelDef.create(Void.class, VERIFY_CHANNEL)
        );
    }

    // Atomically read/write/send message in RPC
    @RPC(
            dataAttributesLoadingType = P
    )
    public String verify(Context context, Persistence persistence, Communication communication) {
        String status = persistence.getDataAttribute(DA_Status, String.class);
        if (status == "verified") {
            return "already verified";
        }
        persistence.setDataAttribute(DA_Status, "verified");
        communication.publishInternalChannel(VERIFY_CHANNEL, null);
        return "done";
    }
}
