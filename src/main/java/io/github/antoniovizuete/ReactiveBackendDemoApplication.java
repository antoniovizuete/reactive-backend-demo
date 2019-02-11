package io.github.antoniovizuete;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ReactiveBackendDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReactiveBackendDemoApplication.class, args);
  }

  @Bean
  public Map<String, SseEmitter> emitters() {
    return new HashMap<>();
  }
}

