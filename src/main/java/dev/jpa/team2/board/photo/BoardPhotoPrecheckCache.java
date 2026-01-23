package dev.jpa.team2.board.photo;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BoardPhotoPrecheckCache {

  public record CacheValue(boolean allowed, String reasonCode, String reasonText, Double score, long expiresAtMillis) {}

  private final Map<String, CacheValue> store = new ConcurrentHashMap<>();

  // TTL은 5~10분 추천
  private final long ttlMillis = Duration.ofMinutes(10).toMillis();

  public void put(Long memberId, String sha256, boolean allowed, String reasonCode, String reasonText, Double score) {
    if (memberId == null || sha256 == null) return;
    long exp = System.currentTimeMillis() + ttlMillis;
    store.put(key(memberId, sha256), new CacheValue(allowed, reasonCode, reasonText, score, exp));
  }

  public CacheValue get(Long memberId, String sha256) {
    if (memberId == null || sha256 == null) return null;
    CacheValue v = store.get(key(memberId, sha256));
    if (v == null) return null;
    if (v.expiresAtMillis() < System.currentTimeMillis()) {
      store.remove(key(memberId, sha256));
      return null;
    }
    return v;
  }

  private String key(Long memberId, String sha256) {
    return memberId + ":" + sha256;
  }
}
