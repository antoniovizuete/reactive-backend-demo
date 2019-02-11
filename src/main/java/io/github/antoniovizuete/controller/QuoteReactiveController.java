package io.github.antoniovizuete.controller;

import java.util.Map;

import io.github.antoniovizuete.domain.Quote;
import io.github.antoniovizuete.repository.QuoteMongoReactiveRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.github.antoniovizuete.services.QuijoteDataLoader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class QuoteReactiveController {

  private QuijoteDataLoader quijoteDataLoader;

  private Map<String, SseEmitter> emitters;

  private QuoteMongoReactiveRepository quoteMongoReactiveRepository;

  public QuoteReactiveController(final QuijoteDataLoader quijoteDataLoader, final Map<String, SseEmitter> emitters, final QuoteMongoReactiveRepository quoteMongoReactiveRepository) {
    this.quijoteDataLoader = quijoteDataLoader;
    this.emitters = emitters;
    this.quoteMongoReactiveRepository = quoteMongoReactiveRepository;
  }

  @GetMapping("/init-process")
  @CrossOrigin("")
  public String initProcess() {
    return quijoteDataLoader.load();
  }

  @GetMapping("/progress/{id}")
  public SseEmitter getProgress(@PathVariable final String id) {
    return emitters.get(id);
  }

  @GetMapping("/quotes")
  public Flux<Quote> getQuotes() {
    return quoteMongoReactiveRepository.findAll();
  }

  @DeleteMapping("/quotes/all")
  public Mono<Void> deleteAllQuotes() {
    return quoteMongoReactiveRepository.deleteAll();
  }

}
