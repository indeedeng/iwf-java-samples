# iwf-java-samples

Samples for [iWF Java SDK](https://github.com/indeedeng/iwf-java-sdk) that runs
against [iWF server](https://github.com/indeedeng/iwf)

## Setup

1. Start a iWF server following the [instructions](https://github.com/indeedeng/iwf#how-to-run-this-server)
2. Run this project by using gradle task `bootRun`.

_Note that by default this project will listen on 8803 port

## Product Use case samples


### [JobSeeker Engagement workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/engagement)
<img width="709" alt="Screenshot 2023-04-21 at 8 53 25 AM" src="https://user-images.githubusercontent.com/4523955/233680837-6a6267a0-4b31-419e-87f0-667bb48582d1.png">


See [Engagement](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/engagement)
for
how to build an jobSeeker engagement workflow.


* An engagement is initiated by an employer to reach out to a jobSeeker(via email/SMS/etc)
* The jobSeeker could respond with decline or accept
* If jobSeeker doesn't respond, it will get reminder
* An engagement can change from declined to accepted, but cannot change from accepted to declined

### [Job Post System (a mini Indeed.com)](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/jobpost)

See [JobPost](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/jobpost) for
how to build an JobPost system like Indeed.com

Support typical CRUD operations:

* Create a job with tile, description and notes
* Read a job
* Update a job
* Delete a job

And also

* Search for jobs using full-text search
* Update will trigger a background action to update external system with eventual consistency

### Subscription workflow

See [Subscription](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/subscription)
with [unit tests](https://github.com/indeedeng/iwf-java-samples/tree/main/src/test/java/io/iworkflow/workflow/subscription)
for the use case also described in:

* [Temporal TypeScript tutorials](https://learn.temporal.io/tutorials/typescript/subscriptions/)
* [Temporal go sample](https://github.com/temporalio/subscription-workflow-project-template-go)
* [Temporal Java Sample](https://github.com/temporalio/subscription-workflow-project-template-java)
* [Cadence Java example](https://cadenceworkflow.io/docs/concepts/workflows/#example)

In additional, iWF provides "Auto-ContinueAsNew feature to allow running the workflow infinitely

