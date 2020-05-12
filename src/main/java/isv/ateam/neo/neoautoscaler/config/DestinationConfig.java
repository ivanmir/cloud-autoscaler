package isv.ateam.neo.neoautoscaler.config;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Base64Utils;

import com.sap.core.connectivity.api.configuration.ConnectivityConfiguration;
import com.sap.core.connectivity.api.configuration.DestinationConfiguration;

@Configuration
public class DestinationConfig {
	
	private final static String PLATFORM_API_DESTINATION_NAME = "platform_api";
	public final static String HEADER_CSRF = "X-CSRF-Token";
	public static DestinationConfiguration platformAPIDestination;
	public static String baseLMUrl = "";
	public static String baseOAuthUrl = "";
	public static String baseMonUrl = "";
	public static String baseCSRFUrl = "";
	public static String basicAuthentication;
	public static String encodedClientData;
	public static String clientID;
	public static String clientSecret;
	public static String scope;
	public static String cpuLoad;
	public static String busyThreads;
	

	@Bean("DestInit")
	public static void initialize() {
		
		try {
			Context ctx = new InitialContext();
			ConnectivityConfiguration configuration = (ConnectivityConfiguration) ctx.lookup("java:comp/env/connectivityConfiguration");
 
			platformAPIDestination = configuration.getConfiguration(PLATFORM_API_DESTINATION_NAME);
			
			String user = platformAPIDestination.getProperty("User");
			String password = platformAPIDestination.getProperty("Password");
			String auth = user + ":" + password;
			byte[] encodedAuth = Base64Utils.encode(auth.getBytes());

			basicAuthentication = "Basic " + new String(encodedAuth);

			baseLMUrl = platformAPIDestination.getProperty("URL") + "/lifecycle/v1/accounts/";
			baseOAuthUrl = platformAPIDestination.getProperty("URL") + "/oauth2/apitoken/v1";
			baseMonUrl = platformAPIDestination.getProperty("URL") + "/monitoring/v1/accounts/";			
			baseCSRFUrl = platformAPIDestination.getProperty("URL") + "/lifecycle/v1/csrf/";
			clientID = platformAPIDestination.getProperty("clientID");
			clientSecret = platformAPIDestination.getProperty("clientSecret");
			scope = platformAPIDestination.getProperty("scope");
			cpuLoad = platformAPIDestination.getProperty("CPULoad");
			busyThreads = platformAPIDestination.getProperty("BusyThreads");
			
			encodedClientData = "Basic " + Base64Utils.encodeToString(( clientID + ":" + clientSecret).getBytes());

		} catch (NamingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
