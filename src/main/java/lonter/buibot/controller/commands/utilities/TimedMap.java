package lonter.buibot.controller.commands.utilities;

import lombok.val;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TimedMap<K, V> {
  private final Map<K, ExpiringValue<V>> map = new ConcurrentHashMap<>();

  public void put(final @NotNull K k, final @NotNull V v, final long expiration) {
    cleanup();
    map.put(k, new ExpiringValue<>(v, System.currentTimeMillis()+expiration));
  }

  public @NotNull Optional<V> get(final @NotNull K k) {
    cleanup();

    val entry = map.get(k);
    return entry == null ? Optional.empty() : Optional.of(entry.value());
  }

  public void remove(final @NotNull K k) {
    map.remove(k);
  }

  private void cleanup() {
    map.entrySet().removeIf(i -> i.getValue().expired());
  }

  private record ExpiringValue<V>(@NotNull V value, long expireAt) {
    public boolean expired() {
      return System.currentTimeMillis() > expireAt;
    }
  }
}