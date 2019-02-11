package io.github.antoniovizuete.services.refactor;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

//@Service
public class SseEmitterService {

  private Map<String, SseEmitter> emitters;

  public SseEmitterService(final Map<String, SseEmitter> emitters) {
    this.emitters = emitters;
  }

  public String createEmitter() {
    final SseEmitter sseEmitter = new SseEmitter(600_000L);
    final String idEmitter = UUID.randomUUID().toString();

    sseEmitter.onCompletion(() -> emitters.remove(idEmitter));

    emitters.put(idEmitter, sseEmitter);
    return idEmitter;
  }


}
