package io.iworkflow.patterns.workflow.scalableparallel.models;

import java.util.List;

public record BatchEnqueueRequest(
        List<String> list
) {
}