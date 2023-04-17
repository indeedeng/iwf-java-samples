# iwf-java-samples

Samples for [iWF Java SDK](https://github.com/indeedeng/iwf-java-sdk) that runs
against [iWF server](https://github.com/indeedeng/iwf)

## Setup

1. Start a iWF server following the [instructions](https://github.com/indeedeng/iwf#how-to-run-this-server)
2. Run this project by using gradle task `bootRun`.

_Note that by default this project will listen on 8803 port

## Product Use case samples

### Engagement workflow

This [Engagement](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/engagement) how to build an jobSeeker engagement workflow.

* An engagement is initiated by an employer to reach out to a jobSeeker(via email/SMS/etc) 
* The jobSeeker could respond with decline or accept
* If jobSeeker doesn't respond, it will get reminder
* An engagement can change from declined to accepted, but cannot change from accepted to declined


### Subscription workflow

This [Subscription](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/subscription)
with [unit tests](https://github.com/indeedeng/iwf-java-samples/tree/main/src/test/java/io/iworkflow/workflow/subscription)
This use case is also described in:

* [Temporal TypeScript tutorials](https://learn.temporal.io/tutorials/typescript/subscriptions/)
* [Temporal go sample](https://github.com/temporalio/subscription-workflow-project-template-go)
* [Temporal Java Sample](https://github.com/temporalio/subscription-workflow-project-template-java)
* [Cadence Java example](https://cadenceworkflow.io/docs/concepts/workflows/#example)

In additional, iWF provides "Auto-ContinueAsNew feature to allow running the workflow infinitely

