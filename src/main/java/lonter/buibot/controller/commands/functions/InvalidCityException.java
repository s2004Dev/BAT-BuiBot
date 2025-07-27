package lonter.buibot.controller.commands.functions;

import org.jetbrains.annotations.NotNull;

public final class InvalidCityException extends Exception {
  public InvalidCityException(final @NotNull String message) {
    super(message);
  }
}