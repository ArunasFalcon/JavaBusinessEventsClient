# JavaBusinessEventsClient
Demo project how to listen to Dynamics 365 FinOps business events coming over a service bus topic and pull relevant entity data from FO via OData in Java.

This is a minimal example of a client listening on an Azure service bus topic for Dynamics 365 for Finance and Supply Chain Management (FO) business events of the type "free text invoice posted".
This business event is present in the standard application. After receiving a message the client then pulls the rest of the invoice header data from the FO OData interface.

# Prerequisites
- You need Java to run the client, and the Azure service bus and MSAL libraries.
- You need an FO instance.
- You need an Azure service bus with a topic and a subscription to that topic.
- You need to set up a service bus topic endpoint in FO and point it to the topic you created.
- You need to activate the free text invoice posted event and configure it to send to the endpoint from the previous step.
- You need to create a service principal in the same AAD tenant to which your FO instance is assigned, assign it a secret and the application permissions to "Microsoft ERP" (there's just one). Then enter the service principal app id into the form "Azure AD applications" in FO.

# Running the client
The main class is TSTopicListener. The args are:
- 0: connection string to the service bus (root level listen permissions)
- 1: name of the subscription (typically this is topicName/subscriptions/subscriptionName)
- 2: base uri of the FO instance without trailing slash, so it looks like this: https://myinstancename.axcloud.dynamics.com
- 3: guid of the FO/service principal tenant
- 4: app id of the service principal
- 5: service principal secret

# Notes

This is just a minimal demo, it's hardcoded to assume the business events coming over the topic are free text invoice events, and it's also hardcoded to attempt to pull free text invoice header data from FO.
