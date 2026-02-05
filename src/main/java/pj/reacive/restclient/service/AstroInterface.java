package pj.reacive.restclient.service;

import org.springframework.web.service.annotation.GetExchange;
import pj.reacive.restclient.json.AstroResponse;
import reactor.core.publisher.Mono;

public interface AstroInterface {
    @GetExchange("/astros.json")
    Mono<AstroResponse> getAstroResponse();
}
