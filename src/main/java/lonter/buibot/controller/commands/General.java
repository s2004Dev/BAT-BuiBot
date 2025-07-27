package lonter.buibot.controller.commands;

import static lonter.buibot.controller.commands.Util.*;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.bat.annotations.Command;
import lonter.bat.annotations.CommandClass;
import lonter.bat.annotations.help.Help;
import lonter.bat.annotations.help.Subcommand;
import lonter.bat.annotations.parameters.ats.Args;
import lonter.bat.annotations.parameters.ats.Event;
import lonter.buibot.controller.bot.SharedResources;
import lonter.buibot.controller.commands.functions.BirthdayService;
import lonter.buibot.controller.commands.functions.InvalidCityException;
import lonter.buibot.controller.commands.functions.XPManager;
import lonter.buibot.model.mappers.UserMapper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.MonthDay;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@CommandClass @AllArgsConstructor
public class General {
  private static final Logger log = LoggerFactory.getLogger(General.class);

  private final UserMapper userMapper;
  private final XPManager xpManager;
  private final BirthdayService birthdayService;
  private final SharedResources shared;

  @Command @Help(description = "Bui will send the birth day of said user.", usage = "[id]")
  @Subcommand(name = "set", description = "Bui will ask you to set your birthday information.",
    usage = "<dd/MM timezone>")
  public @NotNull String birthday(final @Args String @NotNull[] args,
                                  final @Event @NotNull MessageReceivedEvent e) {
    if(args.length > 0 && args[0].equals("set")) {
      val wrongFormat = "Bui! Wrong format! `" + shared.prefix + "birthday set dd/MM timezone`.";

      if(args.length < 3)
        return wrongFormat;

      val split = args[1].split("/");

      if(split.length != 2)
        return wrongFormat;

      try {
        val day = Integer.parseInt(split[0]);
        val month = Integer.parseInt(split[1]);

        MonthDay.of(month, day);

        birthdayService.setBirthday(e.getAuthor().getIdLong(), day, month, args[2]);

        return "Bui! Now I know your birthday!";
      }

      catch(final @NotNull Exception ex) {
        return switch(ex) {
          case DateTimeException _ -> "Bui! That day does not exist!";
          case InvalidCityException _ -> ex.getMessage();

          default -> {
            ex.printStackTrace();
            yield wrongFormat;
          }
        };
      }
    }

    val id = getUserId(args, e);

    if(self(id, e))
      return "Bui! My next birthday is " + date("09", "01") + " (Europe/Rome)! <:Star:1100387219975442502>";

    if(id < 1)
      return sendMessageMention(id);

    val found = userMapper.findBirthdayById(id);
    val idk = "Bui! I don't know this user birthday...";

    if(found.isEmpty())
      return idk;

    val member = e.getGuild().getMemberById(id);

    if(member == null) {
      log.warn("birthday(): No member found with id {}.", id);
      return "Bui! Something went wrong...";
    }

    val user = found.get();
    val birthday = user.birthday;
    val timezone = user.timezone;

    if(birthday == null || timezone == null) {
      log.warn("birthday(): No birthday information found null for id {}.", id);
      return idk;
    }

    return "Bui! " + genitive(member.getEffectiveName()) + " next birthday is " +
      date(String.valueOf(birthday.getDayOfMonth()), String.valueOf(birthday.getMonth().getValue())) +
      " (" + timezone + ")!";
  }

  @Command @Help(description = "Bui will send the current latency.")
  public @NotNull String ping(final @Event @NotNull MessageReceivedEvent e) {
    return "Bui! My ping is: **" + e.getJDA().getGatewayPing() + "ms**.";
  }

  @Command(aliases = "lb") @Help(description = "Bui will send the list of all the bui things sent.", usage = "<args>")
  @Subcommand(name = "bui", description = "Bui will send the list of the people who said bui the most.")
  @Subcommand(name = "buizel", description = "Bui will send the list of the people who said buizel the most.")
  @Subcommand(name = "levels", description = "Bui will send the list of the people who talked the most.")
  public @NotNull Object leaderboard(final @Args String @NotNull[] args,
                                     final @Event @NotNull MessageReceivedEvent e) {
    if(args.length < 1)
      return "Bui... you have to specify what do you want me to show the list of!";

    val type = args[0];

    if(!List.of("bui", "buizel", "levels").contains(type))
      return "Bui! I don't get the input...";

    val embed = new EmbedBuilder();

    embed.setTitle("Bui! Here are the people who " + (type.equals("levels") ? "talk" : "said" + type) +
      " the most!");

    val text = new StringBuilder();
    val idCaller = e.getAuthor().getIdLong();
    val notInTop = new AtomicBoolean(true);
    val users = userMapper.findAllForRank(type.equals("levels") ? "xps" : type);
    val i = new AtomicInteger();

    users.forEach(user -> {
      val line = new StringBuilder();

      val num = switch(type) {
        case "bui" -> user.bui;
        case "buizel" -> user.buizel;
        case "levels" -> xpManager.getLevel(user.id);
        default -> 0;
      };

      if(idCaller == user.id)
        notInTop.set(false);

      val member = e.getGuild().getMemberById(user.id);

      if(member == null) {
        log.warn("leaderboard(): member {} was found null.", user.id);
        return;
      }

      line.append("**").append(i.incrementAndGet()).append(") ").append(member.getEffectiveName()).append(":** ")
        .append(num).append("\n");

      text.append(line);
    });

    val cut = userMapper.getCount()-users.size();

    if(cut != 0)
      text.append("\n\n***").append(cut == 1 ? "A line was" : cut + " lines were").append(" cut.***");

    if(notInTop.get()) {
      var place = userMapper.getIndex(idCaller);

      if(place == 0)
        place = users.size()+1;

      text.append("\n**You're at the ").append(place).append(switch(place%10) {
        case 1 -> "st";
        case 2 -> "nd";
        case 3 -> "rd";
        default -> "th";
      }).append(" place.**");
    }

    return embed.setDescription(text.toString()).setFooter("Bui, remember to say bui!");
  }

  @Command(value = "profilepicture", aliases = "pfp")
  @Help(description = "Bui will send someone's profile picture.", usage = "[id] | [args] [id]")
  @Subcommand(name = "local", description = "Bui will send someone's local profile picture", usage = "[id]")
  public @NotNull Object profilePicture(final @Args String @NotNull[] args,
                                        final @Event @NotNull MessageReceivedEvent e) {
    val embed = new EmbedBuilder();
    val id = getUserId(args.length > 0 && args[0].equals("local") ? removeFirst(args) : args, e);

    var member = e.getMember();

    if(member == null)
      return "Bui... an error has occurred...";

    if(id < 1)
      return args[0].equals("local") ? member.getAvatarId() == null ? "Bui! You don't have a local profile picture!" :
        embed.setTitle("Bui! Here is your current local profile picture!").setImage(member.getAvatarUrl() +
          "?size=2048") : sendMessageMention(id);

    if(id == e.getAuthor().getIdLong())
      return args.length > 0 && args[0].equals("local") ? member.getAvatarId() == null ? "Bui! You don't have a " +
        "local profile picture!" : embed.setTitle("Bui! Here is your current local profile picture!")
        .setImage(member.getAvatarUrl() + "?size=2048") : embed.setTitle("Bui! Here is your current profile picture!")
        .setImage("https://cdn.discordapp.com/avatars/" + id + "/" + e.getJDA().retrieveUserById(id).complete()
          .getAvatarId() + ".png?size=2048");

    member = e.getGuild().getMemberById(id);

    return member == null ? "Bui... an error has occurred..." : args[0].equals("local") ?
      member.getAvatarId() == null ? "Bui! " + member.getEffectiveName() + " doesn't have a local profile picture!" :
        embed.setTitle("Bui! Here is " + genitive(member.getEffectiveName()) + " local profile picture!")
          .setImage(member.getAvatarUrl() + "?size=2048") : embed.setTitle("Bui! Here is " +
      genitive(member.getEffectiveName()) + " profile picture!").setImage("https://cdn.discordapp.com/avatars/" + id + "/" +
      e.getJDA().retrieveUserById(id).complete().getAvatarId() + ".png?size=2048");
  }

  @Command @Help(description = "Bui will send someone's rank card in the server (by messages).", usage = "[id]")
  public @NotNull Object rank(final @Args String @NotNull[] args, final @Event @NotNull MessageReceivedEvent e) {
    val id = getUserId(args, e);

    if(self(id, e))
      return "Bui! I am unrankable! <:Chad:1045753361737199656>";

    if(id < 1)
      return sendMessageMention(id);

    if(!userMapper.exists(id))
      return "Bui! I don't know this user...";

    val lvl = xpManager.getLevel(id);
    val xp = xpManager.getXP(id);
    val xpNext = xpManager.getXpFromLevel(xpManager.getLevel(id)+1);
    val xpThisLvl = xpManager.getXpFromLevel(lvl);
    val progress = map(xp, xpThisLvl, xpNext, 0, 100);
    val barLength = 10;

    var filled = progress*barLength/100;
    var empty = barLength-filled;

    if(filled < 0) {
      filled = 0;
      empty = 10;
    }

    if(empty < 0) {
      filled = 10;
      empty = 0;
    }

    val user = e.getJDA().getUserById(id);

    return user == null ? "Bui! I don't know this user..." : new EmbedBuilder().setTitle(e.getAuthor()
        .getIdLong() == id ? "Bui! Here your rank card!" : "Bui! Here is " + genitive(user.getEffectiveName()) +
        "'s rank card!").setThumbnail(user.getAvatarUrl()).setDescription("**Lvl:** " + lvl + " | **" +
      (xp-xpThisLvl) + "** / " + (xpNext-xpThisLvl) + " **XPs** - (" + (xpNext-xp) + " XPs left)\n\n" +
      ":green_square:".repeat(filled) + ":white_large_square:".repeat(empty) + " - (" + progress + "%)")
      .setFooter("Please do not spam!");
  }

  @Command @Help(description = "Bui will send the amount of times someone said bui things.", usage = "[id]")
  public @NotNull Object stats(final @Args String @NotNull[] args, final @Event @NotNull MessageReceivedEvent e) {
    val id = getUserId(args, e);

    if(id < 1)
      return sendMessageMention(id);

    if(self(id, e))
      return "Bui! I am unrankable! <:Chad:1045753361737199656>";

    val member = e.getJDA().getUserById(id);

    if(member == null)
      return "Bui! Something went wrong...";

    val user = userMapper.findById(id).orElseGet(( ) -> userMapper.insert(id));
    val embed = new EmbedBuilder();

    if(id == e.getAuthor().getIdLong())
      embed.setTitle("Here are your stats:").setDescription("You said \"Bui\" " + user.getBui() + " time" +
        plural(user.getBui()) + ".\nYou also said \"Buizel\" " + user.getBuizel() + " time" +
        plural(user.getBuizel()) + ".");

    else
      embed.setTitle("Here are " + genitive(member.getEffectiveName()) + " stats:").setDescription("They said " +
        "\"Bui\" " + user.getBui() + " time" + plural(user.getBui()) + ".\nThey also said \"Buizel\" " +
        user.getBuizel() + " time" + plural(user.getBuizel()) + ".");

    return embed.setFooter("Bui! Great job!").setThumbnail(member.getAvatarUrl());
  }
}