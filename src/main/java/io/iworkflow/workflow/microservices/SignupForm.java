package io.iworkflow.workflow.microservices;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableSignupForm.class)
public abstract class SignupForm {
    public abstract String getUsername();

    public abstract String getEmail();

    public abstract String getFirstName();

    public abstract String getLastName();
}
