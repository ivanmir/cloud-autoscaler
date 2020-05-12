package isv.ateam.neo.neoautoscaler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

	private static final int SECONDS = 7200;

	public WebClientConfig() {
	}

	HttpClient getConnector() {
		
		HttpClient httpClient = HttpClient.create()
				.tcpConfiguration(client ->
						client.doOnConnected(conn -> conn
								.addHandlerLast(new ReadTimeoutHandler(SECONDS))
								.addHandlerLast(new WriteTimeoutHandler(SECONDS))));
		return httpClient;
	}

	@Bean("apiLMClient")
	@DependsOn("DestInit")
	public WebClient apiLMClient(WebClient.Builder webClientBuilder) {
		return webClientBuilder
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.baseUrl(DestinationConfig.baseLMUrl)
				.clientConnector(new ReactorClientHttpConnector(getConnector()))
				.build();
	}

	@Bean("apiMonClient")
	@DependsOn("DestInit")
	public WebClient apiMonClient(WebClient.Builder webClientBuilder) {
		return webClientBuilder
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.baseUrl(DestinationConfig.baseMonUrl)
				.clientConnector(new ReactorClientHttpConnector(getConnector()))
				.build();
	}

	@Bean(name = "apiOAuthClient")
	@DependsOn("DestInit")
	public WebClient apiOAuthClient(WebClient.Builder webClientBuilder) {
		return webClientBuilder
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.baseUrl(DestinationConfig.baseOAuthUrl)
				.clientConnector(new ReactorClientHttpConnector(getConnector()))
				.build();
	}

	@Bean("apiCSRFClient")
	@DependsOn("DestInit")
	public WebClient apiCSRFClient(WebClient.Builder webClientBuilder) {
		return webClientBuilder
				.defaultHeader(DestinationConfig.HEADER_CSRF, "Fetch")
				.baseUrl(DestinationConfig.baseCSRFUrl)
				.clientConnector(new ReactorClientHttpConnector(getConnector()))
				.build();
	}

}
