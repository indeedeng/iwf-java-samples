package io.iworkflow.patterns.workflow.parentchild;

public record WaitForChildInput(
        String childWFId, int timerSeconds
){};
