package isv.ateam.neo.neoautoscaler.config;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.web.reactive.function.client.WebClient;

public class UserAgentCustomizer implements WebClientCustomizer {

	  @Override
	  public void customize(WebClient.Builder webClientBuilder) {
	    webClientBuilder.defaultHeader("User-Agent", "MY-APPLICATION");
	  }

}
