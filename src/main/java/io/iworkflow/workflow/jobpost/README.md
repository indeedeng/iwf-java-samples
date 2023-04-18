* Also
  see [EngagementController](https://github.com/indeedeng/iwf-java-samples/blob/main/src/main/java/io/iworkflow/controller/EngagementWorkflowController.java)
  for how to implement APIs based on this workflow implementation

How to test the APIs in browser:

* start API: http://localhost:8803/jobpost/create?title="Software Engineer"&description="in Seattle"
    * It will return the workflowId which can be used in subsequence API calls.
* update API: http://localhost:8803/jobpost/update?workflowId=<TODO>&title="Senior Software Engineer"&description="in
  Portland"&notes=testnotes
* search API, use queries like:
    * ['Title="software" AND JobDescription="Seattle" ORDER BY LastUpdateTimeMillis '](http://localhost:8803/engagement/list?query='Title="software"
      AND JobDescription="Seattle" ORDER BY LastUpdateTimeMillis ')
    * etc

### Search attribute requirement

If using Temporal:

* New CLI

```bash
tctl search-attribute create -name Title -type Text -y
tctl search-attribute create -name JobDescription -type Text -y
tctl search-attribute create -name LastUpdateTimeMillis -type Int -y
```

* Old CLI

``` bash
tctl adm cl asa -n Title -t Text
tctl adm cl asa -n JobDescription -t Text
tctl adm cl asa -n LastUpdateTimeMillis -t Int

```

If using Cadence

```bash
cadence adm cl asa --search_attr_key Title --search_attr_type 0
cadence adm cl asa --search_attr_key JobDescription --search_attr_type 0
cadence adm cl asa --search_attr_key LastUpdateTimeMillis --search_attr_type 2
```