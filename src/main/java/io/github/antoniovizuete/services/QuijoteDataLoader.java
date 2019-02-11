package io.github.antoniovizuete.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.antoniovizuete.domain.Quote;
import io.github.antoniovizuete.repository.QuoteMongoReactiveRepository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class QuijoteDataLoader {

  private static final Logger log = LoggerFactory.getLogger(QuijoteDataLoader.class);

  private QuoteMongoReactiveRepository quoteMongoReactiveRepository;

  private Map<String, SseEmitter> emmiters;

  QuijoteDataLoader(final QuoteMongoReactiveRepository quoteMongoReactiveRepository,
      final Map<String, SseEmitter> emmiters) {
    this.quoteMongoReactiveRepository = quoteMongoReactiveRepository;
    this.emmiters = emmiters;
  }

  public String load() {
    final SseEmitter sseEmitter = new SseEmitter(600_000L);
    final String idEmitter = UUID.randomUUID().toString();
    final LoadRunnable loader = new LoadRunnable(new AtomicLong(0L), idEmitter, sseEmitter, quoteMongoReactiveRepository);

    Executors.newSingleThreadExecutor().execute(loader);

    sseEmitter.onCompletion(() -> emmiters.remove(idEmitter));

    emmiters.put(idEmitter, sseEmitter);

    return idEmitter;
  }

  @AllArgsConstructor
  @Slf4j
  static final class LoadRunnable implements Runnable {

    private AtomicLong lineNumber;
    private String idEmitter;
    private SseEmitter sseEmitter;
    private QuoteMongoReactiveRepository repository;


    @Override
    public void run() {
      BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("pg2000.txt")));
      final Long linesCount = bufferedReader.lines().filter(line -> !line.trim().isEmpty()).count();

      bufferedReader =
        new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("pg2000.txt")));

      bufferedReader.lines().filter(line -> !line.trim().isEmpty()).forEach(line -> {
        final Quote quote =
          repository.save(new Quote(lineNumber.addAndGet(1), idEmitter, "El Quijote", line)).block();
        final Integer progress = (int) (((double)quote.getLine() / linesCount) * 100);

        if ( sseEmitter != null ) {
          try {
            sseEmitter.send(SseEmitter.event().data(progress));
          } catch (IOException e) {
            sseEmitter.completeWithError(e);
          } catch (IllegalStateException e) {
            sseEmitter = null;
          }
          if (sseEmitter != null && progress == 100) {
            sseEmitter.complete();

          }
        }

        log.info("New quote loaded: {}", quote);
      });

      log.info("Repository contains now {} entries.", repository.count().block());
    }
  }

}
