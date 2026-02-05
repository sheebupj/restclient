package pj.reacive.restclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import pj.reacive.restclient.service.AstroInterface;

@Configuration
public class AppConfig {
    @Bean
    public AstroInterface astroInterface() {
        var webClient = WebClient.create("http://api.open-notify.org/");
        var adapter = WebClientAdapter.create(webClient);
        var factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AstroInterface.class);
    }
}
