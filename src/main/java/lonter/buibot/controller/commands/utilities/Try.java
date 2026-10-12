package lonter.buibot.controller.commands.utilities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@NoArgsConstructor
public abstract class Try<V> {
  public static <V> @NotNull Optional<V> tryf(final @NotNull Callable<V> action,
                                              final @NotNull Consumer<Exception> error) {
    return call(action).ifFailure(error).toOptional();
  }

  public static <V> @NotNull Optional<V> tryn(final @NotNull Callable<V> action) {
    return call(action).toOptional();
  }

  public static <V> @NotNull Try<V> call(final @NotNull Callable<V> action) {
    return Try.of(( ) -> success(action.call()));
  }

  public static <V> @NotNull Try<V> success(final @NotNull V value) {
    return new Success<>(value);
  }

  public static <V> @NotNull Try<V> failure(final @NotNull Exception cause) {
    return new Failure<>(cause);
  }

  private static <V> @NotNull Try<V> of(final @NotNull Callable<Try<V>> action) {
    try {
      return action.call();
    }

    catch(final @NotNull Exception e) {
      return failure(e);
    }
  }

  public abstract @Nullable V get() throws Exception;

  public abstract Try<V> ifFailure(final @NotNull Consumer<Exception> causeConsumer);

  public abstract Optional<V> toOptional();

  @AllArgsConstructor
  private static class Success<V> extends Try<V> {
    private final @Nullable V value;

    @Override public @Nullable V get() {
      return value;
    }

    @Override public @NotNull Try<V> ifFailure(final @NotNull Consumer<Exception> causeConsumer) {
      return this;
    }

    @Override public @NotNull Optional<V> toOptional() {
      return Optional.ofNullable(get());
    }

    @Override public boolean equals(final @Nullable Object that) {
      if(this == that)
        return true;

      if(that == null || getClass() != that.getClass())
        return false;

      return Objects.equals(value, ((Success<?>) that).value);
    }

    @Override public int hashCode() {
      return Objects.hash(value);
    }
  }

  @AllArgsConstructor
  private static class Failure<V> extends Try<V> {
    private final @NotNull Exception cause;

    @Override public @Nullable V get() throws Exception {
      throw cause;
    }

    @Override public @NotNull Try<V> ifFailure(final @NotNull Consumer<Exception> causeConsumer) {
      causeConsumer.accept(cause);
      return this;
    }

    @Override public @NotNull Optional<V> toOptional() {
      return Optional.empty();
    }

    @Override public boolean equals(final @Nullable Object that) {
      if(this == that)
        return true;

      if(that == null || getClass() != that.getClass())
        return false;

      return Objects.equals(cause, ((Failure<?>) that).cause);
    }

    @Override public int hashCode() {
      return Objects.hash(cause);
    }
  }
}