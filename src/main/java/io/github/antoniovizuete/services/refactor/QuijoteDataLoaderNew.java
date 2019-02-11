package io.github.antoniovizuete.services.refactor;

import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

//@Service
public class QuijoteDataLoaderNew {

  private static final Logger log = LoggerFactory.getLogger(QuijoteDataLoaderNew.class);

  private SseEmitterService sseEmitterService;

  private ApplicationContext context;

  QuijoteDataLoaderNew(final ApplicationContext context, final SseEmitterService sseEmitterService) {
    this.sseEmitterService = sseEmitterService;
    this.context = context;
  }

  public String load() {
    final QuijoteLoaderRunnableNew loader = context.getBean(QuijoteLoaderRunnableNew.class);
    loader.setIdEmitter(sseEmitterService.createEmitter());

    Executors.newSingleThreadExecutor().execute(loader);

    return loader.getIdEmitter();
  }

}
