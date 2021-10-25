# custom-incident-handler
This project illustrates how to add custom incident handlers to the CAMUNDA Platform
based on https://docs.camunda.org/manual/latest/user-guide/process-engine/incidents/

![Configuration of incident handling](/resources/images/incidentHandlingConfiguration.png)
If the extension property `signalIncident` and `signalName` are set on the service task,
then the custom incident handler will send a signal or the given name including error
information to any processes subscribed to this signal.

When the incident occurs (and no BPMNError is thrown), 
a regular Camunda failed job incident will be created.
![Incident occurred](/resources/images/incidentOccurred.png)

However, the custom incident handler [MyFailedJobIncidentHandler](./src/main/java/com/camunda/example/incident/MyFailedJobIncidentHandler.java)
will send the configured signal, which can initiate an [incident handling process](./src/main/resources/incident-handling.bpmn)

![Incident Handling](/resources/images/incidentHandling.png)

After the incident handling has been completed, the incident is resolved by incrementing the retries in [RetryDelegate](./src/main/java/com/camunda/example/service/RetryDelegate.java) 

![Incident resolved](/resources/images/incidentResolved.png)