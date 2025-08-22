package io.iworkflow.patterns.workflow.waitforstatecompletion;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableJobSeekerData.class)
public abstract class JobSeekerData {
    public abstract Integer getId();

    @Value.Default
    public String getName() {
        return "Test Job Seeker";
    }

    @Value.Default
    public String getResume() {
        return "Test Resume";
    }

    @Value.Default
    public String getEmail() {
        return "testjobseeker@indeed.com";
    }
}
