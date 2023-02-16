package tsbustest;

import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class TSTopicListener {
	
	private String finOpsInstance;
	private String tenantId;
	private String appId;
	private String secret;
	FOClient foClient;
	MessageHandler handler;
	ServiceBusProcessorClient subClient;

	public static void main(String[] args) throws InterruptedException, ServiceBusException {
		TSTopicListener listener = new TSTopicListener(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
		listener.run();
	}
	
	public TSTopicListener(
			String _connectionString,
			String _topicName,
			String _subscription,
			String _finOpsInstance,
			String _tenantId,
			String _appId,
			String _secret
			) throws InterruptedException, ServiceBusException {
		finOpsInstance = _finOpsInstance;
		tenantId = _tenantId;
		appId = _appId;
		secret = _secret;
		foClient = new FOClient(tenantId, appId, secret, finOpsInstance);
		handler = new MessageHandler(foClient);
		subClient = new ServiceBusClientBuilder()
				.connectionString(_connectionString)
				.processor()
				.topicName(_topicName)
				.subscriptionName(_subscription)
				.processMessage(t -> handler.onMessageAsync(t))
				.processError(t -> handler.processError(t))
				.buildProcessorClient();
	}
	
	public void run() throws InterruptedException, ServiceBusException {
		subClient.start();
	}

}

class MessageHandler{
	
	FOClient foClient;
	
	public MessageHandler(FOClient client) {
		foClient = client;
	}

	public CompletableFuture<Void> onMessageAsync(ServiceBusReceivedMessageContext context) {
		var message = context.getMessage();
		String messageText = message.getBody().toString();
		System.out.println("Received message: " + messageText);
		JSONObject event = new JSONObject(messageText);
		String dataAreaId = event.getString("BusinessEventLegalEntity");
		String invoiceId = event.getString("InvoiceId");
		try {
			String invoiceData = foClient.getInvoiceData(dataAreaId, invoiceId);
			System.out.println(invoiceData);
		} catch (Exception e) {
			System.out.println("Error retrieving FO invoice data: " + e.getMessage());
		}
		return null;
	}

	public void processError(ServiceBusErrorContext context) {
	    System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
	    context.getFullyQualifiedNamespace(), context.getEntityPath());
	    System.out.printf("Exception occurred: %s%n", context.getException());
	    return;
	}
	
}