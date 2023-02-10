package tsbustest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;

public class FOClient {
	
	private String tenantId;
	private String clientId;
	private String clientSecret;
	private String foBaseUri;
	
	public FOClient(String _tenantId, String _clientId, String _clientSecret, String _foBaseUri) {
		this.tenantId = _tenantId;
		this.clientId = _clientId;
		this.clientSecret = _clientSecret;
		this.foBaseUri = _foBaseUri;
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		FOClient client = new FOClient(args[0], args[1], args[2], args[3]);
		String data = client.getInvoiceData("USMF", "FTI-00000024");
		System.out.println(data);
	}
	
	public String getToken() throws MalformedURLException, InterruptedException, ExecutionException {
		var authority = "https://login.microsoftonline.com/" + tenantId;
		var scope = foBaseUri + "/.default";
		
		var clientApplication = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(clientSecret))
		.authority(authority)
		.build();

		var clientCredentialParam = ClientCredentialParameters.builder(Collections.singleton(scope))
		.build();

		var result = clientApplication.acquireToken(clientCredentialParam).get();
		var accessToken = result.accessToken();
		System.out.println("Got access token");
		return accessToken.toString();
	}
	
	public String getInvoiceData(String dataAreaId, String invoiceId) throws IOException, InterruptedException, ExecutionException {
		String uri = foBaseUri + "/data/FreeTextInvoiceHeaders?cross-company=true&$filter=" + URLEncoder.encode("FreeTextNumber eq '" + invoiceId + "' and dataAreaId eq '" + dataAreaId + "'","UTF-8");
		URL url = new URL(uri);
		System.out.println(url);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		String authVal = "Bearer " + getToken();
		System.out.println(authVal);
		con.setRequestProperty("Authorization", authVal);
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "*/*");
		InputStream in = con.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder result = new StringBuilder();
		String line;
		while((line = reader.readLine()) != null) {
		    result.append(line);
		}
		return result.toString();
	}
}

