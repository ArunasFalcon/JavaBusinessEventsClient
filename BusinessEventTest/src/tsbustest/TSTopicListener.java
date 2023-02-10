package tsbustest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;

import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class TSTopicListener {
	
	private String connectionString;
	private SubscriptionClient subClient;
	private String finOpsInstance;
	private String tenantId;
	private String appId;
	private String secret;
	private String subId;
	FOClient foClient;

	public static void main(String[] args) throws InterruptedException, ServiceBusException {
		TSTopicListener listener = new TSTopicListener(args[0], args[1], args[2], args[3], args[4], args[5]);
		listener.run();
	}
	
	public TSTopicListener(String _connectionString,
			String _subscription,
			String _finOpsInstance,
			String _tenantId,
			String _appId,
			String _secret
			) throws InterruptedException, ServiceBusException {
		connectionString = _connectionString;
		finOpsInstance = _finOpsInstance;
		tenantId = _tenantId;
		appId = _appId;
		secret = _secret;
		subId = _subscription;
		foClient = new FOClient(tenantId, appId, secret, finOpsInstance);
		subClient = new SubscriptionClient(new ConnectionStringBuilder(connectionString,subId), ReceiveMode.PEEKLOCK);
	}
	
	public void run() throws InterruptedException, ServiceBusException {
		subClient.registerMessageHandler(new MessageHandler(foClient));
	}

}

class MessageHandler implements IMessageHandler{
	
	FOClient foClient;
	
	public MessageHandler(FOClient client) {
		foClient = client;
	}

	@Override
	public CompletableFuture<Void> onMessageAsync(IMessage message) {
		String messageText = new String(message.getBody(),StandardCharsets.UTF_8);
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

	@Override
	public void notifyException(Throwable exception, ExceptionPhase phase) {
		System.out.println(phase + " encountered exception: " + exception.getMessage());
	}
	
}