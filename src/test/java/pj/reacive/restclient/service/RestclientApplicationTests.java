package pj.reacive.restclient.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pj.reacive.restclient.json.AstroResponse;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RestclientApplicationTests {

	@Autowired
    private AstroService service;

    @Test
    void getPeopleInSpace(){
        String people= service.getPeopleInSpace();
        assertNotNull(people);
        assertTrue(people.contains("people"));
         System.out.println(people);
    }
    @Test
    void getAstroResponseSync() {
        AstroResponse response = service.getAstroResponseSync();
        assertNotNull(response);
        assertEquals("success", response.message());
        assertTrue(response.number() >= 0);
        assertEquals(response.number(), response.people().size());
        System.out.println(response);
    }
    @Test
    void getAstroResponseAsync(){
        AstroResponse response=service.getAstroResponseAsync()
                .block(Duration.ofSeconds(10));
        assertNotNull(response);
        assertEquals("success",response.message());
        assertTrue(response.number()>=0);
        assertEquals(response.number(),response.people().size());
    }
    @Test
    void getAstroResponseAsuncStepVerifier(){
        service.getAstroResponseAsync()
                .as(StepVerifier::create)
                .assertNext(response->{
                    assertNotNull(response);
                    assertEquals("success",response.message());
                    assertTrue(response.number()>=0);
                    assertEquals(response.number(),response.people().size());
                })
                .verifyComplete();
    }
    @Test
    void getAstroResponseFromInterface(@Autowired AstroInterface astroInterface) {
        AstroResponse response = astroInterface.getAstroResponse()
                .block(Duration.ofSeconds(2));
        assertNotNull(response);
        assertAll(
                () -> assertEquals("success", response.message()),
                () -> assertTrue(response.number() >= 0),
                () -> assertEquals(response.number(), response.people().size())
        );
        System.out.println(response);
    }
    @Test
    void testSchedulers() {
        System.out.println("Test starting on: " + Thread.currentThread().getName());

        String result = service.saveAstronautsToFile()
                .doOnSubscribe(s -> System.out.println("Subscribed on: " + Thread.currentThread().getName()))
                .block();

        assertNotNull(result);
        assertTrue(result.contains("astronauts"));

        // Check that file was created
        assertTrue(Files.exists(Paths.get("astronauts.json")));
    }
    @Test
    void testSubscribeOn() {
        System.out.println("Test starting on: " + Thread.currentThread().getName());

        Integer count = service.countAstronautsWithLogging()
                .block();

        assertTrue(count >= 0);
        
    }

}
