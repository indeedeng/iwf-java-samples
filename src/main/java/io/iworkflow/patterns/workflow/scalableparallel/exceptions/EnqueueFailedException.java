package io.iworkflow.patterns.workflow.scalableparallel.exceptions;

public class EnqueueFailedException extends RuntimeException {
    public EnqueueFailedException(String message) {
        super(message);
    }
}
