package io.iworkflow.controller;

import com.google.common.collect.ImmutableMap;
import io.iworkflow.core.Client;
import io.iworkflow.core.ImmutableWorkflowOptions;
import io.iworkflow.gen.models.WorkflowSearchResponse;
import io.iworkflow.workflow.engagement.EngagementWorkflow;
import io.iworkflow.workflow.jobpost.ImmutableJobUpdateInput;
import io.iworkflow.workflow.jobpost.JobPostWorkflow;
import io.iworkflow.workflow.jobpost.JobUpdateInput;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/jobpost")
public class JobPostController {

    private final Client client;

    public JobPostController(
            final Client client
    ) {
        this.client = client;
    }

    @GetMapping("/create")
    public ResponseEntity<String> start(
            @RequestParam String title,
            @RequestParam String description
    ) {
        final String wfId = "jobpost_test_id_" + System.currentTimeMillis() / 1000;

        client.startWorkflow(EngagementWorkflow.class, wfId, 3600, null,
                ImmutableWorkflowOptions.builder()
                        .initialSearchAttribute(ImmutableMap.of(
                                JobPostWorkflow.SA_KEY_TITLE, title,
                                JobPostWorkflow.SA_KEY_JOB_DESCRIPTION, description,
                                JobPostWorkflow.SA_KEY_LAST_UPDATE_TIMESTAMP, System.currentTimeMillis() / 1000
                        ))
                        .build());

        return ResponseEntity.ok(String.format("started workflowId: %s", wfId));
    }

    @GetMapping("/update")
    public ResponseEntity<String> optout(
            @RequestParam String workflowId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(defaultValue = "test-notes") String notes
    ) {
        final JobPostWorkflow rpcStub = client.newRpcStub(JobPostWorkflow.class, workflowId, "");
        JobUpdateInput input = ImmutableJobUpdateInput.builder()
                .description(description)
                .title(title)
                .notes(notes)
                .build();
        client.invokeRPC(rpcStub::update, input);

        return ResponseEntity.ok("declined");
    }

    @GetMapping("/list")
    public ResponseEntity<WorkflowSearchResponse> list(
            @RequestParam String query
    ) {
        if (query.startsWith("'")) {
            query = query.substring(1, query.length() - 1);
        }
        System.out.println("got query for search: " + query);
        // this is just a shortcut for demo for how flexible the search can be
        // in real world you may want to provide some search patterns like listByEmployerId+status etc
        WorkflowSearchResponse response = client.searchWorkflow(query, 1000);

        return ResponseEntity.ok(response);
    }
}