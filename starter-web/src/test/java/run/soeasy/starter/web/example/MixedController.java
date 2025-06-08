package run.soeasy.starter.web.example;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class MixedController {

    @GetMapping("/mvc/hello")
    public String mvcHello() {
        return "Hello, MVC!";
    }

    @GetMapping("/mixed/hello")
    public Mono<String> mixedHello() {
        return Mono.just("Hello from both worlds!");
    }
}