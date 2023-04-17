

### Search attribute requirement

Using Temporal:

* New CLI
```bash
tctl adm cl asa -n ProposeUserId -t Keyword
tctl adm cl asa -n TargetUserId -t Keyword
tctl adm cl asa -n Status -t Keyword
tctl adm cl asa -n LastUpdateTimeMillis -t Int
```

* Old CLI
``` bash
tctl search-attribute create -name ProposeUserId -type Keyword -y
tctl search-attribute create -name TargetUserId -type Keyword -y
tctl search-attribute create -name Status -type Keyword -y
tctl search-attribute create -name LastUpdateTimeMillis -type Int -y
```

If using Cadence

```bash
cadence adm cl asa --search_attr_key ProposeUserId --search_attr_type 1
cadence adm cl asa --search_attr_key TargetUserId --search_attr_type 1
cadence adm cl asa --search_attr_key Status --search_attr_type 1
cadence adm cl asa --search_attr_key LastUpdateTimeMillis --search_attr_type 2
```