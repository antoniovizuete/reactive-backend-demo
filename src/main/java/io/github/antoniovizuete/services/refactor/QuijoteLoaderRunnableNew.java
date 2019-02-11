package io.github.antoniovizuete.services.refactor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.github.antoniovizuete.domain.Quote;
import io.github.antoniovizuete.repository.QuoteMongoReactiveRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
//@Component
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class QuijoteLoaderRunnableNew implements Runnable {

  @Getter @Setter
  private String idEmitter;

  private AtomicLong lineNumber = new AtomicLong(0L);

  private QuoteMongoReactiveRepository repository;

  private Map<String, SseEmitter> emitters;

  @Autowired
  public QuijoteLoaderRunnableNew(final QuoteMongoReactiveRepository repository, Map<String, SseEmitter> emitters) {
    this.repository = repository;
    this.emitters = emitters;
  }


  @Override
  public void run() {

    final InputStream is = getClass().getClassLoader().getResourceAsStream("pg2000.txt");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

    final Long linesCount = bufferedReader.lines().filter(line -> !line.trim().isEmpty()).count();

    new BufferedReader(new InputStreamReader(is)).lines().filter(line -> !line.trim().isEmpty()).forEach(line -> {
      SseEmitter sseEmitter = emitters.get(idEmitter);
      final Quote quote = repository.save(new Quote(lineNumber.addAndGet(1), idEmitter, "El Quijote", line)).block();
      final Integer progress = (int) (((double) quote.getLine() / linesCount) * 100);
      if (sseEmitter != null) {
        try {
          quote.getLine();
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
