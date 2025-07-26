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
import lonter.buibot.model.mappers.UserMapper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@CommandClass @AllArgsConstructor
public class General {
  private final UserMapper userMapper;
  private final XPManager xpManager;

  @Command @Help(description = "Bui will send the birth day of said user.")
  public @NotNull String birthday(final @Args String @NotNull[] args,
                                  final @Event @NotNull MessageReceivedEvent e) {
    val id = getUserId(args, e);

    if(id < 1L)
      return sendMessageMention(id);

    val found = userMapper.findBirthdayById(id);

    if(found.isEmpty())
      return "Bui! I don't know this user birthday...";

    val user = e.getGuild().getMemberById(id);

    return user == null ? "Bui! Something went wrong..." :
      "Bui! " + user.getEffectiveName() + "'s next birthday is " + found.get();
  }

  @Command @Help(description = "Bui will send the current latency.")
  public @NotNull String ping(final @Event @NotNull MessageReceivedEvent e) {
    return "Bui! My ping is: **" + e.getJDA().getGatewayPing() + "ms**.";
  }

  @Command @Help(description = "Bui will send the list of every bui things sent.")
  @Subcommand(name = "bui", description = "") @Subcommand(name = "buizel", description = "")
  @Subcommand(name = "bevels", description = "")
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
    val users = userMapper.findAllForRank(type);
    val i = new AtomicInteger();

    users.forEach(user -> {
      val line = new StringBuilder();

      val num = switch(type) {
        case "bui" -> user.bui;
        case "buizel" -> user.buizel;
        case "levels" -> xpManager.getLevel(user.xps);
        default -> 0;
      };

      if(idCaller == user.id)
        notInTop.set(false);

      val member = e.getGuild().getMemberById(user.id);

      if(member == null) {
        System.out.println("Member " + user.id + " was found null.");
        return;
      }

      line.append("**").append(i.getAndIncrement()).append(") ").append(member.getEffectiveName()).append(":** ")
        .append(num).append("\n");

      text.append(line);
    });

    text.append("\n\n***").append(userMapper.getCount()-users.size()).append(" lines were cut.***");

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

    embed.setDescription(text.toString());
    embed.setFooter("Bui, remember to say bui!");

    return embed;
  }

  @Command @Help(description = "Bui will send someone's profile picture.")
  public @NotNull Object profilePicture(final @Args String @NotNull[] args,
                                        final @Event @NotNull MessageReceivedEvent e) {
    if(!e.isFromGuild())
      return "Bui! You must be in a guild to use this command!";

    val embed = new EmbedBuilder();
    val id = getUserId(args, e);

    var member = e.getMember();

    if(member == null)
      return "Bui... an error has occurred...";

    if(id < 1L)
      return args[0].equals("local") ? member.getAvatarId() == null ? "Bui! You don't have a local profile picture!" :
        embed.setTitle("Bui! Here is your current local profile picture!").setImage(member.getAvatarUrl() +
          "?size=4096&quality=lossless") : sendMessageMention(id);

    if(id == e.getAuthor().getIdLong()) {
      if(args.length > 1 && args[1].equalsIgnoreCase("local")) {
        if(member.getAvatarId() == null)
          return "Bui! You don't have a local profile picture!";

        return embed.setTitle("Bui! Here is your current local profile picture!")
          .setImage(member.getAvatarUrl() + "?size=2048");
      }

      return embed.setTitle("Bui! Here is your current profile picture!")
        .setImage("https://cdn.discordapp.com/avatars/" + id + "/" +
          e.getJDA().retrieveUserById(id).complete().getAvatarId() + ".png" + "?size=2048");
    }

    member = e.getGuild().getMemberById(id);

    if(member == null)
      return "Bui... an error has occurred...";

    if(args.length > 1 && args[1].equalsIgnoreCase("local")) {
      if(member.getAvatarId() == null)
        return "Bui! " + member.getEffectiveName() + " doesn't have a local profile picture!";

      return embed.setTitle("Bui! Here is " + member.getEffectiveName() + " local profile picture!")
        .setImage(member.getAvatarUrl() + "?size=2048");
    }

    return embed.setTitle("Bui! Here is " + member.getEffectiveName() + " profile picture!")
      .setImage("https://cdn.discordapp.com/avatars/" + id + "/" +
        e.getJDA().retrieveUserById(id).complete().getAvatarId() + ".png" + "?size=2048");
  }

  @Command @Help(description = "Bui will send your ranking in the Server (by messages).")
  public @NotNull Object rank(final @Args String @NotNull[] args, final @Event @NotNull MessageReceivedEvent e) {
    val id = getUserId(args, e);

    if(id < 1L)
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

    if(user == null)
      return "Bui! I don't know this user...";

    return new EmbedBuilder().setTitle(e.getAuthor().getIdLong() == id ? "Bui! Here your rank card!" :
      "Bui! Here is " + user.getName() + "'s rank card!").setThumbnail(user.getAvatarUrl())
      .setDescription("**Lvl:** " + lvl + " | **" + (xp - xpThisLvl) + "** / " + (xpNext - xpThisLvl) +
      " **XPs** - (" + (xpNext - xp) + " XPs left) \n\n" + ":green_square:".repeat(filled) +
      ":white_large_square:".repeat(empty) + " - (" + progress + "%)").setFooter("Please do not spam!");
  }

  @Command @Help(description = "Bui will send the amount of times someone said bui things.")
  public @NotNull Object stats(final @NotNull MessageReceivedEvent e) {
    if(!e.isFromGuild())
      return "You can't use this command in DM.";

    val found = userMapper.findById(e.getAuthor().getIdLong());

    if(found.isEmpty())
      return "Bui! Something went wrong...";

    val user = found.get();

    return new EmbedBuilder().setTitle(e.getAuthor().getName() + "'s stats:")
      .setDescription("You said bui " + user.getBui() + " times. \nYou also said Buizel " + user.getBuizel() +
        " times.").setFooter("Bui! Great job!");
  }
}