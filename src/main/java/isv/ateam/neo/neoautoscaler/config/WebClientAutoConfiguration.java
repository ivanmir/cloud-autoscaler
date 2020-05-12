package isv.ateam.neo.neoautoscaler.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.ClientHttpConnectorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientCodecCustomizer;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(WebClient.class)
@AutoConfigureAfter({ CodecsAutoConfiguration.class, ClientHttpConnectorAutoConfiguration.class })
public class WebClientAutoConfiguration {

  private final WebClient.Builder webClientBuilder;

  public WebClientAutoConfiguration(ObjectProvider<WebClientCustomizer> customizerProvider) {
    this.webClientBuilder = WebClient.builder();
    customizerProvider.orderedStream().forEach((customizer) -> customizer.customize(this.webClientBuilder));
  }

  @Bean
  @Scope("prototype")
  @ConditionalOnMissingBean
  public WebClient.Builder webClientBuilder() {
    return this.webClientBuilder.clone();
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnBean(CodecCustomizer.class)
  protected static class WebClientCodecsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @Order(0)
    public WebClientCodecCustomizer exchangeStrategiesCustomizer(List<CodecCustomizer> codecCustomizers) {
      return new WebClientCodecCustomizer(codecCustomizers);
    }

  }

}
