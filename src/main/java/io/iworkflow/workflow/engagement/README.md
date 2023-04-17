
* Also see [EngagementController](https://github.com/indeedeng/iwf-java-samples/blob/main/src/main/java/io/iworkflow/controller/EngagementWorkflowController.java) for how to implement APIs based on this workflow implementation

How to test the APIs in browser:
* start API: http://localhost:8803/engagement/start
  * It will return the workflowId which can be used in subsequence API calls. 
* describe API: http://localhost:8803/engagement/describe?workflowId=<TODO>
* opt-out email API: http://localhost:8803/engagement/optout?workflowId=<TODO>
* decline API: http://localhost:8803/engagement/decline?workflowId=<TODO>&notes=%22not%20interested%22
* accept API: http://localhost:8803/engagement/accept?workflowId=<TODO>&notes=%27accept%27
* search API, use queries like:
  * ['EmployerId="test-employer" ORDER BY LastUpdateTimeMillis '](http://localhost:8803/engagement/list?query=<TODO>)
  * ['EmployerId="test-employer"'](http://localhost:8803/engagement/list?query=<TODO>)
  * ['EmployerId="test-employer" AND EngagementStatus="INITIATED"'](http://localhost:8803/engagement/list?query=<TODO>)
  * etc
### Search attribute requirement

If using Temporal:

* New CLI
```bash
tctl search-attribute create -name EmployerId -type Keyword -y
tctl search-attribute create -name JobSeekerId -type Keyword -y
tctl search-attribute create -name EngagementStatus -type Keyword -y
tctl search-attribute create -name LastUpdateTimeMillis -type Int -y
```

* Old CLI
``` bash
tctl adm cl asa -n EmployerId -t Keyword
tctl adm cl asa -n JobSeekerId -t Keyword
tctl adm cl asa -n Status -t Keyword
tctl adm cl asa -n LastUpdateTimeMillis -t Int

```

If using Cadence

```bash
cadence adm cl asa --search_attr_key EmployerId --search_attr_type 1
cadence adm cl asa --search_attr_key JobSeekerId --search_attr_type 1
cadence adm cl asa --search_attr_key Status --search_attr_type 1
cadence adm cl asa --search_attr_key LastUpdateTimeMillis --search_attr_type 2
```