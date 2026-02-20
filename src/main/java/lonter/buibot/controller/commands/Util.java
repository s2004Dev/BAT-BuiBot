package lonter.buibot.controller.commands;

import lombok.val;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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

      try {
        val user = e.getJDA().getUserByTag(input);

        return user == null ? countOccurrences(input, "#") == 1 && input.split("#")[1].matches("-?\\d+") ? 0 : -1 :
          user.getIdLong();
      }

      catch(final @NotNull Exception ignored) {
        return -1;
      }
    }
  }

  public static boolean self(final long id, final @NotNull MessageReceivedEvent e) {
    return id == e.getJDA().getSelfUser().getIdLong();
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
    val current = ThreadLocalRandom.current();

    return switch(min) {
      case Integer _ when max instanceof Integer ->
        (T) Integer.valueOf(current.nextInt(min.intValue(), max.intValue()+1));

      case Double _ when max instanceof Double ->
        (T) Double.valueOf(current.nextDouble(min.doubleValue(), max.doubleValue()));

      case Float _ when max instanceof Float ->
        (T) Float.valueOf(current.nextFloat(min.floatValue(), max.floatValue()));

      case Long _ when max instanceof Long ->
        (T) Long.valueOf(current.nextLong(min.longValue(), max.longValue()+1));

      default -> throw new IllegalArgumentException("Unsupported type: " + min.getClass());
    };
  }

  public static <T extends Number> @NotNull String plural(final @Nullable T input) {
    return switch(input) {
      case Byte i -> i == 1 ? "" : "s";
      case Short i -> i == 1 ? "" : "s";
      case Integer i -> i == 1 ? "" : "s";
      case Long i -> i == 1 ? "" : "s";
      case Float i -> i == 1. ? "" : "s";
      case Double i -> i == 1. ? "" : "s";
      case null, default -> "s";
    };
  }

  public static void send(final @NotNull String message, final @NotNull MessageReceivedEvent e) {
    e.getChannel().sendMessage(message).queue();
  }

  public static int map(final int x, final int inMin, final int inMax, final int outMin,
                          final int outMax) {
    return (x-inMin)*(outMax-outMin)/(inMax-inMin)+outMin;
  }

  public static String @NotNull[] removeFirst(final String @NotNull[] input) {
    return removeTo(input, 1);
  }

  public static String @NotNull[] removeTo(final String @NotNull[] input, final int index) {
    val newArray = new String[input.length-index];
    System.arraycopy(input, index, newArray, 0, newArray.length);
    return newArray;
  }

  public static @NotNull String genitive(final @NotNull String input) {
    return input + (input.endsWith("s") ? "'" : "'s");
  }

  public static @NotNull String date(@NotNull String day, @NotNull String month, final @NotNull ZoneId timezone) {
    if(day.length() == 1)
      day = "0" + day;

    if(month.length() == 1)
      month = "0" + month;

    val dm = day + "/" + month + "/";
    val year = (Year.now().getValue());
    val date = dm + year;

    try {
      return new Date(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay(timezone)
        .toInstant().toEpochMilli()).after(new Date()) ? date : dm + (year+1);
    }

    catch(final @NotNull Exception e) {
      throw new RuntimeException(e);
    }
  }
}