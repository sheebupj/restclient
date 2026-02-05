package pj.reacive.restclient.service;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import pj.reacive.restclient.json.AstroResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AstroService {
    private final RestClient restclient;
    private final WebClient webclient;

    public AstroService() {
        this.webclient = WebClient.create("http://api.open-notify.org");
        this.restclient = RestClient.create("http://api.open-notify.org");
    }

    public String getPeopleInSpace(){
        return restclient.get()
                .uri("/astros.json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }
    public AstroResponse getAstroResponseSync() {
        return restclient.get()
                .uri("/astros.json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(AstroResponse.class);
    }
    public Mono<AstroResponse> getAstroResponseAsync(){
        return webclient.get()
                .uri("/astros.json")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AstroResponse.class)
                .log();
    }
    public Mono<String> badFileOperation() {
        return getAstroResponseAsync()
                .map(response -> {
                    try {
                        // This is a blocking I/O operation!
                        Path file = Paths.get("astronauts.json");
                        Files.writeString(file, response.toString());
                        return "File written with " + response.number() + " astronauts";
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
    public Mono<String> saveAstronautsToFile() {
        return getAstroResponseAsync()
                .publishOn(Schedulers.boundedElastic())  // Switch to I/O thread pool
                .map(response -> {
                    try {
                        // Now this blocking operation runs on the right thread pool
                        Path file = Paths.get("astronauts.json");
                        Files.writeString(file, response.toString());
                        return "File written with " + response.number() + " astronauts";
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .doOnNext(result -> System.out.println("Thread: " + Thread.currentThread().getName()));
    }
    public Mono<Integer> countAstronautsWithLogging() {
        return Mono.fromCallable(() -> {
                    System.out.println("Starting on thread: " + Thread.currentThread().getName());
                    return "Starting computation";
                })
                .subscribeOn(Schedulers.boundedElastic())  // Entire chain starts here
                .flatMap(msg -> getAstroResponseAsync())
                .map(response -> {
                    System.out.println("Processing on thread: " + Thread.currentThread().getName());
                    return response.number();
                })
                .doOnNext(count -> System.out.println("Count: " + count + " on thread: " + Thread.currentThread().getName()));
    }
    public Mono<String> demonstratePublishOn() {
        return Mono.fromCallable(() -> {
                    System.out.println("1. Source: " + Thread.currentThread().getName());
                    return "data";
                })
                .map(data -> {
                    System.out.println("2. Before publishOn: " + Thread.currentThread().getName());
                    return data + "-step2";
                })
                .publishOn(Schedulers.boundedElastic())  // ← Switch happens HERE
                .map(data -> {
                    System.out.println("3. After publishOn: " + Thread.currentThread().getName());
                    return data + "-step3";
                })
                .map(data -> {
                    System.out.println("4. Still after publishOn: " + Thread.currentThread().getName());
                    return data + "-step4";
                });
    }
    public Mono<String> demonstrateSubscribeOn() {
        return Mono.fromCallable(() -> {
                    System.out.println("1. Source: " + Thread.currentThread().getName());
                    return "data";
                })
                .map(data -> {
                    System.out.println("2. Transform: " + Thread.currentThread().getName());
                    return data + "-step2";
                })
                .subscribeOn(Schedulers.boundedElastic())  // ← Affects the WHOLE chain
                .map(data -> {
                    System.out.println("3. After subscribeOn: " + Thread.currentThread().getName());
                    return data + "-step3";
                });
    }

}
