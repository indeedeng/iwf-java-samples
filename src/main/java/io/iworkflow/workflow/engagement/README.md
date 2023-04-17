
* Also see [EngagementController](https://github.com/indeedeng/iwf-java-samples/blob/main/src/main/java/io/iworkflow/controller/EngagementWorkflowController.java) for how to implement APIs based on this workflow implementation

How to test the APIs in browser:
* start API: http://localhost:8803/engagement/start
  * It will return the workflowId which can be used in subsequence API calls. 
* describe API: http://localhost:8803/engagement/describe?workflowId=engagement_test_id_1681709110
* opt-out email API: http://localhost:8803/engagement/optout?workflowId=engagement_test_id_1681709110
* decline API: http://localhost:8803/engagement/decline?workflowId=engagement_test_id_1681709110&notes=%22not%20interested%22
* accept API: http://localhost:8803/engagement/accept?workflowId=engagement_test_id_1681709110&notes=%27accept%27
* search API, use queries like:
  * ['ProposeUserId="test-proposer" ORDER BY LastUpdateTimeMillis '](http://localhost:8803/engagement/list?query=%27ProposeUserId=%22test-proposer%22%20ORDER%20BY%20LastUpdateTimeMillis%20%20%27)
  * ['ProposeUserId="test-proposer"'](http://localhost:8803/engagement/list?query=%27ProposeUserId=%22test-proposer%22%20%27)
  * ['ProposeUserId="test-proposer" AND EngagementStatus="INITIATED"'](http://localhost:8803/engagement/list?query=%27ProposeUserId=%22test-proposer%22%20AND%20EngagementStatus=%22INITIATED%22%27)
  * etc
### Search attribute requirement

If using Temporal:

* New CLI
```bash
tctl search-attribute create -name ProposeUserId -type Keyword -y
tctl search-attribute create -name TargetUserId -type Keyword -y
tctl search-attribute create -name EngagementStatus -type Keyword -y
tctl search-attribute create -name LastUpdateTimeMillis -type Int -y
```

* Old CLI
``` bash
tctl adm cl asa -n ProposeUserId -t Keyword
tctl adm cl asa -n TargetUserId -t Keyword
tctl adm cl asa -n Status -t Keyword
tctl adm cl asa -n LastUpdateTimeMillis -t Int

```

If using Cadence

```bash
cadence adm cl asa --search_attr_key ProposeUserId --search_attr_type 1
cadence adm cl asa --search_attr_key TargetUserId --search_attr_type 1
cadence adm cl asa --search_attr_key Status --search_attr_type 1
cadence adm cl asa --search_attr_key LastUpdateTimeMillis --search_attr_type 2
```