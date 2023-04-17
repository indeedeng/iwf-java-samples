package io.iworkflow.workflow.engagement.model;

public enum Status {
    INITIATED,
    ACCEPTED,
    DECLINED, // can still move to INTERESTED later
}
