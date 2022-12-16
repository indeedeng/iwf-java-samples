This sample demonstrate:

1. How to use interstate channel to synchronize multi threading/in parallel workflow execution
2. State0 will go to State1 and State2
3. State1 will wait for a InterStateChannel from State2
4. State2 will send a signal and then finish as a "dead end"