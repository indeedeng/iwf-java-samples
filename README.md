# iwf-java-samples

Samples for [iWF Java SDK](https://github.com/indeedeng/iwf-java-sdk) that runs
against [iWF server](https://github.com/indeedeng/iwf)

## Setup

1. Start a iWF server following the [instructions](https://github.com/indeedeng/iwf#how-to-run-this-server)
2. Run this project by using gradle task `bootRun`.

_Note that by default this project will listen on 8080 port(default Spring port)_

## How to Start sample workflow

1. [Basic IO workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/basic):
   Open http://localhost:8080/basic/start in your browser. This workflow demonstrate:
   * How to start workflow with input and get output
   * How to pass input from a state to a next state
2. [Persistence workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/persistence):
   Open http://localhost:8080/persistence/start in your browser. This workflow demonstrate:
   * How to use data objects to share data across workflows
   * How to use search attributes to share data and also searching for workflows
   * How to use record events API
   * How to use StateLocal to pass data from start to decide API
3. [Signal workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/signal):
   Open http://localhost:8080/signal/start in your browser. This workflow demonstrate:
   * How to use signal
   * How to use AnyCommandCompleted trigger type
   * State1 start API will wait for two signals, when any of them is received, the decide API is trigger
4. [Timer workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/timer):
   Open http://localhost:8080/timer/start in your browser. This workflow demonstrate:
   * How to use a durable timer
   * State1 start API will wait for a timer, when timer fires, the decide API is trigger
5. [InterstateChannel workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/iworkflow/workflow/interstatechannel):
   Open http://localhost:8080/interstateChannel/start in your browser
6. WIP(dsl dynamic workflow)

Then watch the workflow in Cadence or Temporal Web UI
