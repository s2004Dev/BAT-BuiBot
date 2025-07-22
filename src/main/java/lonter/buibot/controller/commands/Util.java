package lonter.buibot.controller.commands;

import lombok.val;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public final class Util {
  public static @NotNull String sendMessageMention(final long id) {
    return switch (id) {
      case 0L -> "Bui! Wrong tag!";
      case -1L -> "Bui! I don't get the input!";
      default -> "Bui! Something went wrong...";
    };
  }

  public static long getUserId(final String @NotNull[] args, final @NotNull MessageReceivedEvent e) {
    if(args.length == 0)
      return e.getAuthor().getIdLong();

    val input = args[0];

    try {
      return Long.parseLong(input.replace("<", "").replace("@", "").replace(">", ""));
    }

    catch(final @NotNull Exception ex) {
      if(!(ex instanceof NumberFormatException))
        return -2;

      val user = e.getJDA().getUserByTag(input);

      return user == null ? countOccurrences(input, "#") == 1 && input.split("#")[1].matches("-?\\d+") ? 0 : -1 :
        user.getIdLong();
    }
  }

  public static int countOccurrences(final @NotNull String haystack, final @NotNull String needle) {
    var count = 0;
    var index = 0;

    while((index = haystack.indexOf(needle, index)) != -1) {
      count++;
      index += needle.length();
    }

    return count;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Number> @NotNull T rand(final @NotNull T min, final @NotNull T max) {
    return switch(min) {
      case Integer _ when max instanceof Integer ->
        (T) Integer.valueOf(ThreadLocalRandom.current().nextInt(min.intValue(), max.intValue()+1));

      case Double _ when max instanceof Double ->
        (T) Double.valueOf(ThreadLocalRandom.current().nextDouble(min.doubleValue(), max.doubleValue()));

      case Float _ when max instanceof Float ->
        (T) Float.valueOf(ThreadLocalRandom.current().nextFloat(min.floatValue(), max.floatValue()));

      case Long _ when max instanceof Long ->
        (T) Long.valueOf(ThreadLocalRandom.current().nextLong(min.longValue(), max.longValue()+1));

      default -> throw new IllegalArgumentException("Unsupported type: " + min.getClass());
    };
  }

  public static void send(final @NotNull String message, final @NotNull MessageReceivedEvent e) {
    e.getChannel().sendMessage(message).queue();
  }
}