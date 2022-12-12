# iwf-java-samples

Samples for [iWF Java SDK](https://github.com/indeedeng/iwf-java-sdk) that runs
against [iWF server](https://github.com/indeedeng/iwf)

## Setup

1. Start a iWF server following the [instructions](https://github.com/indeedeng/iwf#how-to-run-this-server)
2. Run this project by using gradle task `bootRun`.

_Note that by default this project will listen on 8080 port(default Spring port)_

## How to Start sample workflow

1. [Basic IO workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/github/cadenceoss/iwf/workflow/basic):
   Open http://localhost:8080/basic/start in your browser
2. [Persistence workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/github/cadenceoss/iwf/workflow/persistence):
   Open http://localhost:8080/persistence/start in your browser
3. [Signal workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/github/cadenceoss/iwf/workflow/signal):
   Open http://localhost:8080/signal/start in your browser
4. [Timer workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/github/cadenceoss/iwf/workflow/timer):
   Open http://localhost:8080/timer/start in your browser
5. [InterstateChannel workflow](https://github.com/indeedeng/iwf-java-samples/tree/main/src/main/java/io/github/cadenceoss/iwf/workflow/interstatechannel):
   Open http://localhost:8080/interstateChannel/start in your browser
6. WIP(dsl dynamic workflow)

Then watch the workflow in Cadence or Temporal Web UI
