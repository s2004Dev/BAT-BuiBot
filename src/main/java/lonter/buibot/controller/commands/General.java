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
import lonter.bat.batobjs.BatEmbed;
import lonter.bat.batobjs.BatMessageReceivedEvent;
import lonter.buibot.controller.bot.SharedResources;
import lonter.buibot.controller.commands.functions.BirthdayService;
import lonter.buibot.controller.commands.functions.InvalidCityException;
import lonter.buibot.controller.commands.functions.XPManager;
import lonter.buibot.model.entities.ReactionRole;
import lonter.buibot.model.mappers.ReactionRoleMapper;
import lonter.buibot.model.mappers.UserMapper;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@CommandClass @AllArgsConstructor
public class General {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final UserMapper userMapper;
  private final XPManager xpManager;
  private final BirthdayService birthdayService;
  private final SharedResources shared;
  private final ReactionRoleMapper rrMapper;

  @Command @Help(description = "Bui will send the birth day of said user.", usage = "[id]")
  @Subcommand(name = "set", description = "Bui will ask you to set your birthday information.",
    usage = "<dd/MM timezone>")
  public @NotNull String birthday(final @Args String @NotNull[] args,
                                  final @Event @NotNull BatMessageReceivedEvent e) {
    if(e.server == null)
      return "Bui! This command works only in a server!";

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

        birthdayService.setBirthday(e.author.id, day, month, String.join("_", removeTo(args, 2)));

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

    if(self(id, e)) {
      val tz = "Europe/Rome";

      return "Bui! My next birthday is " + date("09", "01", ZoneId.of(tz)) + " (" + tz +
        ")! <:Star:1100387219975442502>";
    }

    if(id < 1)
      return sendMessageMention(id);

    val found = userMapper.findBirthdayById(id);
    val idk = "Bui! I don't know this user birthday...";

    if(found.isEmpty())
      return idk;

    val member = e.server.getMemberById(id);

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

    return "Bui! " + genitive(member.localName) + " next birthday is " +
      date(String.valueOf(birthday.getDayOfMonth()), String.valueOf(birthday.getMonth().getValue()), timezone) +
      " (" + timezone.toString().replace("_", " ") + ")!";
  }

  @Command @Help(description = "Bui will send the current latency.")
  public @NotNull String ping(final @Event @NotNull BatMessageReceivedEvent e) {
    return "Bui! My ping is: **" + e.bat.getPing() + "ms**.";
  }

  @Command(aliases = "lb") @Help(description = "Bui will send the list of all the bui things sent.", usage = "<args>")
  @Subcommand(name = "bui", description = "Bui will send the list of the people who said bui the most.")
  @Subcommand(name = "buizel", description = "Bui will send the list of the people who said buizel the most.")
  @Subcommand(name = "levels", description = "Bui will send the list of the people who talked the most.")
  public @NotNull Object leaderboard(final @Args String @NotNull[] args,
                                     final @Event @NotNull BatMessageReceivedEvent e) {
    if(e.server == null)
      return "Bui! This command works only in a server!";

    if(args.length < 1)
      return "Bui... you have to specify what do you want me to show the list of!";

    val type = args[0];

    if(!List.of("bui", "buizel", "levels").contains(type))
      return "Bui! I don't get the input...";

    val embed = new BatEmbed();

    embed.title = "Bui! Here are the people who " + (type.equals("levels") ? "talk" : "said" + type) +
      " the most!";

    val text = new StringBuilder();
    val idCaller = e.author.id;
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

      val member = e.server.getMemberById(user.id);

      if(member == null) {
        log.warn("leaderboard(): member {} was found null.", user.id);
        return;
      }

      line.append("**").append(i.incrementAndGet()).append(") ").append(member.localName).append(":** ")
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

    embed.description = text.toString();
    embed.footer = "Bui, remember to say bui!";

    return embed;
  }

  @Command(value = "profilepicture", aliases = "pfp")
  @Help(description = "Bui will send someone's profile picture.", usage = "[id] | [args] [id]")
  @Subcommand(name = "local", description = "Bui will send someone's local profile picture", usage = "[id]")
  public @NotNull Object profilePicture(final @Args String @NotNull[] args,
                                        final @Event @NotNull BatMessageReceivedEvent e) {
    if(e.server == null)
      return "Bui! This command works only in a server!";

    val embed = new BatEmbed();
    val id = getUserId(args.length > 0 && args[0].equals("local") ? removeFirst(args) : args, e);

    var member = e.author;

    if(id < 1) {
      if(!args[0].equals("local"))
        return sendMessageMention(id);

      if(!member.hasLocalPfp())
        return "Bui! You don't have a local profile picture!";

      embed.title = "Bui! Here is your current local profile picture!";
      embed.imageUrl = member.localPfpUrl + "?size=2048";

      return embed;
    }

    if(id == e.author.id) {
      if(args.length > 0 && args[0].equals("local")) {
        if(!member.hasLocalPfp())
          return "Bui! You don't have a local profile picture!";

        embed.title = "Bui! Here is your current local profile picture!";
        embed.imageUrl = member.localPfpUrl + "?size=2048";

        return embed;
      }

      embed.title = "Bui! Here is your current profile picture!";

      val user = e.bat.getUserById(id);

      if(user == null)
        return "Bui! An error has occurred!";

      embed.imageUrl = "https://cdn.discordapp.com/avatars/" + id + "/" + user.globalPfpUrl + ".png?size=2048";

      return embed;
    }

    member = e.server.getMemberById(id);

    if(member == null)
      return "Bui... an error has occurred...";

    if(args[0].equals("local")) {
      if(!member.hasLocalPfp())
        return "Bui! " + member.localName + " doesn't have a local profile picture!";

      embed.title = "Bui! Here is " + genitive(member.localName) + " local profile picture!";
      embed.imageUrl = member.localPfpUrl + "?size=2048";

      return embed;
    }

    embed.title = "Bui! Here is " + genitive(member.localName) + " profile picture!";

    val user = e.bat.getUserById(id);

    if(user == null)
      return "Bui! An error has occurred!";

    embed.imageUrl = "https://cdn.discordapp.com/avatars/" + id + "/" + user.globalPfpUrl + ".png?size=2048";

    return embed;
  }

  @Command @Help(description = "Bui will send someone's rank card in the server (by messages).", usage = "[id]")
  public @NotNull Object rank(final @Args String @NotNull[] args, final @Event @NotNull BatMessageReceivedEvent e) {
    if(e.server == null)
      return "Bui! This command works only in a server!";

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

    val user = e.bat.getUserById(id);

    if(user == null)
      return "Bui! I don't know this user...";

    val embed = new BatEmbed();

    embed.title = e.author.id == id ? "Bui! Here your rank card!" : "Bui! Here is " + genitive(user.localName) +
      " rank card!";

    embed.thumbnailUrl = user.localPfpUrl;

    embed.description = "**Lvl:** " + lvl + " | **" + (xp-xpThisLvl) + "** / " + (xpNext-xpThisLvl) +
      " **XPs** - (" + (xpNext-xp) + " XPs left)\n\n" + ":green_square:".repeat(filled) +
      ":white_large_square:".repeat(empty) + " - (" + progress + "%)";

    embed.footer = "Please do not spam!";

    return embed;
  }

  @Command
  public @NotNull Object reaction(final @Args String @NotNull[] args, final @Event @NotNull BatMessageReceivedEvent e) {
    if(shared.owner == null) {
      log.warn("reaction(): owner is null.");
      return "Owner is null.";
    }

    if(e.author.id != shared.owner)
      return "Bui! You don't have access to this command!";

    val def = "Usage: `" + shared.prefix + "reaction <list/add/remove> [...args]`.";

    if(args.length < 1)
      return def;

    return switch(args[0]) {
      case "list" -> {
        if(shared.reactionRoles.isEmpty())
          yield "No active reaction roles.";

        val embed = new BatEmbed();

        embed.title = "Active reaction roles";

        val desc = new StringBuilder("ID | NAME | MESSAGE | ROLE | EMOJI\n\n");

        shared.reactionRoles.forEach(rr ->
          desc.append(rr.id).append(") ").append(rr.name).append(": ").append(rr.messageId).append(" | ")
            .append(rr.roleId).append(" | ").append(rr.emojiId).append("\n"));

        embed.description = desc.toString();

        yield embed;
      }

      case "add" -> {
        if(args.length < 5)
          yield "Usage: `" + shared.prefix + "reaction add <message> <role> <emoji> <...name>`.";

        try {
          rrMapper.insert(new ReactionRole(String.join(" ", removeTo(args, 4)), Long.parseLong(args[1]),
            Long.parseLong(args[2]), args[3]));

          shared.updateReactionRoles();
          yield "Reaction role added correctly.";
        }

        catch(final @NotNull Exception ex) {
          ex.printStackTrace();
          yield "Something went wrong.";
        }
      }

      case "remove" -> {
        if(args.length < 2)
          yield "Usage: `" + shared.prefix + "reaction remove <id>`.";

        try {
          rrMapper.deleteById(Long.parseLong(args[1]));
          shared.updateReactionRoles();
          yield "Reaction role removed correctly.";
        }

        catch(final @NotNull Exception ex) {
          yield "Something went wrong.";
        }
      }

      default -> def;
    };
  }

  @Command @Help(description = "Bui will send the amount of times someone said bui things.", usage = "[id]")
  public @NotNull Object stats(final @Args String @NotNull[] args, final @Event @NotNull BatMessageReceivedEvent e) {
    val id = getUserId(args, e);

    if(id < 1)
      return sendMessageMention(id);

    if(self(id, e))
      return "Bui! I am unrankable! <:Chad:1045753361737199656>";

    val member = e.bat.getUserById(id);

    if(member == null)
      return "Bui! Something went wrong...";

    val user = userMapper.findById(id).orElseGet(( ) -> userMapper.insert(id));
    val embed = new BatEmbed();

    if(id == e.author.id) {
      embed.title = "Here are your stats:";

      embed.description = "You said \"Bui\" " + user.getBui() + " time" + plural(user.getBui()) +
        ".\nYou also said \"Buizel\" " + user.getBuizel() + " time" + plural(user.getBuizel()) + ".";
    }

    else {
      embed.title = "Here are " + genitive(member.localName) + " stats:";

      embed.description = "They said " + "\"Bui\" " + user.getBui() + " time" + plural(user.getBui()) +
        ".\nThey also said \"Buizel\" " + user.getBuizel() + " time" + plural(user.getBuizel()) + ".";
    }

    embed.footer = "Bui! Great job!";
    embed.thumbnailUrl = member.localPfpUrl;

    return embed;
  }
}