package isv.ateam.neo.neoautoscaler.apis;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.gson.JsonObject;

import isv.ateam.neo.neoautoscaler.config.DestinationConfig;
import isv.ateam.neo.neoautoscaler.info.AppProps;
import isv.ateam.neo.neoautoscaler.monitor.RuleEngine;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class PlatformAPIHandler {

	public static String accountName;
	public static String applicationName;

	@Autowired 
	WebClient apiMonClient;
	
	@Autowired 
	WebClient apiLMClient;
	
	@Autowired 
	WebClient apiOAuthClient;
	
	@Autowired 
	WebClient apiCSRFClient;

	public void updateAppInfo(boolean refresh) {
		try {
			String result = getAppInfo().get();
			AppProps.update(result);
		} catch (Exception e) {
			AppProps.update("Error: " + e.getCause());
			log.debug("Error: {}", e.getCause());
		}
	}
	
	public CompletableFuture<String> getAppInfo() throws InterruptedException, ExecutionException {
		
		HttpHeaders h = getNewCSRFToken().get();
		String cSRFtoken = h.getFirst(DestinationConfig.HEADER_CSRF);

		return apiLMClient.get()
				.uri(accountName + "/apps/" + applicationName + "/")
				.headers(header -> header.add(DestinationConfig.HEADER_CSRF, cSRFtoken))
				.headers(header -> header.add("Authorization", DestinationConfig.basicAuthentication))
				.retrieve()
				.bodyToMono(String.class)
				.toFuture();
	}

	public CompletableFuture<HttpHeaders> getNewCSRFToken() {
		return apiCSRFClient.get()
				.headers(header -> header.add("Authorization", DestinationConfig.basicAuthentication))
				.exchange()
				.map(rs -> rs.headers().asHttpHeaders())
				.toFuture();
	}

	@SuppressWarnings("unused")
	private CompletableFuture<JsonObject> getNewOAuth2Token() throws InterruptedException, ExecutionException {

		HttpHeaders h = getNewCSRFToken().get();
		String cSRFtoken = h.getFirst(DestinationConfig.HEADER_CSRF);		

		return apiOAuthClient.post()
				.uri("?grant_type=client_credentials")
				.headers(header -> header.add(DestinationConfig.HEADER_CSRF, cSRFtoken))
				.headers(header -> header.add("Authorization", DestinationConfig.basicAuthentication))
				.retrieve()
				.bodyToMono(JsonObject.class)
				.toFuture();
	}

	public CompletableFuture<String> startApplication() throws InterruptedException, ExecutionException {
		
		//Get a CSRF token and store all cookies retrieved
		HttpHeaders h = getNewCSRFToken().get();
		String cSRFtoken = h.getFirst(DestinationConfig.HEADER_CSRF);
		MultiValueMap<String, String> requestCookies = new LinkedMultiValueMap<String, String>();
		List<String> responseCookies = h.get(HttpHeaders.SET_COOKIE);

		responseCookies.forEach(cookie -> {
			requestCookies.add(HttpHeaders.COOKIE, cookie);
		});

		JsonObject body = new JsonObject();
		body.addProperty("applicationState", "STARTED");
		body.addProperty("loadBalancerState","ENABLED");

		String sBody = body.toString();
	    Mono<String> mono = Mono.just(sBody);

		return apiLMClient.put()
			.uri(accountName + "/apps/" + applicationName + "/" + "state")
			.headers(header -> header.add(DestinationConfig.HEADER_CSRF, cSRFtoken))
			.headers(header -> header.addAll(requestCookies) )
			.header(HttpHeaders.CONTENT_LENGTH,
	                mono.map(s -> String.valueOf(s.getBytes(StandardCharsets.UTF_8).length)).block())
			.body(BodyInserters.fromValue(sBody))
			.retrieve()
			.bodyToMono(String.class)
			.toFuture();
	}

	public CompletableFuture<String> stopApplicationProcess(String pid2Stop) throws InterruptedException, ExecutionException {

		//Get a CSRF token and store all cookies retrieved
		HttpHeaders h = getNewCSRFToken().get();
		String cSRFtoken = h.getFirst(DestinationConfig.HEADER_CSRF);
		MultiValueMap<String, String> requestCookies = new LinkedMultiValueMap<String, String>();
		List<String> responseCookies = h.get(HttpHeaders.SET_COOKIE);

		responseCookies.forEach(cookie -> {
			requestCookies.add(HttpHeaders.COOKIE, cookie);
		});

		RuleEngine.loadRuleThresholds();
		
		JsonObject body = new JsonObject();
		body.addProperty("processState", "STOPPED");
		body.addProperty("loadBalancerState","ENABLED");
		body.addProperty("desc_enum","ENABLED");

		String sBody = body.toString();
	    Mono<String> mono = Mono.just(sBody);

	    return apiLMClient.put()
			.uri(accountName + "/apps/" + applicationName + "/processes/" + pid2Stop + "/state")
			.headers(header -> header.add(DestinationConfig.HEADER_CSRF, cSRFtoken))
			.headers(header -> header.addAll(requestCookies) )
			.header(HttpHeaders.CONTENT_LENGTH,
	                mono.map(s -> String.valueOf(s.getBytes(StandardCharsets.UTF_8).length)).block())
			.body(BodyInserters.fromValue(sBody))
			.retrieve()
			.bodyToMono(String.class)
			.toFuture();

	}	

	public CompletableFuture<String> readCurrentMetricsForProcess(String pid) throws InterruptedException, ExecutionException {

		HttpHeaders h = getNewCSRFToken().get();
		String cSRFtoken = h.getFirst(DestinationConfig.HEADER_CSRF);		

		return apiMonClient.get()
				.uri(accountName + "/apps/" + applicationName + "/" + "processes/" + pid + "/metrics")
				.headers(header -> header.add(DestinationConfig.HEADER_CSRF, cSRFtoken))
				.headers(header -> header.add("Authorization", DestinationConfig.basicAuthentication))
				.retrieve()
				.bodyToMono(String.class)
				.toFuture();
	}
}
