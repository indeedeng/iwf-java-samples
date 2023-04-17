# iwf-java-samples

Samples for [iWF Java SDK](https://github.com/indeedeng/iwf-java-sdk) that runs
against [iWF server](https://github.com/indeedeng/iwf)

## Setup

1. Start a iWF server following the [instructions](https://github.com/indeedeng/iwf#how-to-run-this-server)
2. Run this project by using gradle task `bootRun`.

_Note that by default this project will listen on 8803 port

## Product Use case samples

### Subscription workflow

This [Subscription](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/subscription)
with [unit tests](https://github.com/indeedeng/iwf-java-samples/tree/main/src/test/java/io/iworkflow/workflow/subscription)
This use case is also described in:

* [Temporal TypeScript tutorials](https://learn.temporal.io/tutorials/typescript/subscriptions/)
* [Temporal go sample](https://github.com/temporalio/subscription-workflow-project-template-go)
* [Temporal Java Sample](https://github.com/temporalio/subscription-workflow-project-template-java)
* [Cadence Java example](https://cadenceworkflow.io/docs/concepts/workflows/#example)

In additional, iWF provides "Auto-ContinueAsNew feature to allow running the workflow infinitely

tctl adm cl asa -n ProposeUserId -t Keyword
tctl adm cl asa -n TargetUserId -t Keyword
tctl adm cl asa -n Status -t Keyword
tctl adm cl asa -n LastUpdateTimeMillis -t Int

tctl search-attribute create -name ProposeUserId -type Keyword -y
tctl search-attribute create -name TargetUserId -type Keyword -y
tctl search-attribute create -name Status -type Keyword -y
tctl search-attribute create -name LastUpdateTimeMillis -type Int -y