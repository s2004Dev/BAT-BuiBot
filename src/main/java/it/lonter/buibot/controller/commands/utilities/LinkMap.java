package it.lonter.buibot.controller.commands.utilities;

import static it.lonter.buibot.controller.commands.utilities.Util.rand;

import lombok.val;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class LinkMap {
  private static final long EXPIRATION = 3*60*1_000;
  private static final int DIGITS = 6;

  private final TimedMap<String, Link> links = new TimedMap<>();

  public @NotNull String put(final @NonNull Link link) {
    val k = getKey();
    links.put(k, link, EXPIRATION);
    return k;
  }

  private @NotNull String getKey() {
    String k;

    do {
      k = getDigits();
    } while(get(k).isPresent());

    return k;
  }

  private @NotNull String getDigits() {
    val ret = new StringBuilder();

    for(var i = 0; i < DIGITS; i++)
      ret.append(rand(0, 9));

    return ret.toString();
  }

  public @NotNull Optional<Link> get(final @NotNull String k) {
    return links.get(k);
  }

  public void remove(final @NotNull String k) {
    links.remove(k);
  }

  public record Link(long idFrom, long idTo, @NotNull String source) { }
}