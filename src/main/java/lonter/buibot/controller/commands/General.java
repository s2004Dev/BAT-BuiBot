package lonter.buibot.controller.commands;

import static lonter.buibot.controller.commands.Util.*;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.bat.annotations.Command;
import lonter.bat.annotations.CommandClass;
import lonter.bat.annotations.help.Help;
import lonter.bat.annotations.parameters.ats.Args;
import lonter.bat.annotations.parameters.ats.Event;
import lonter.buibot.model.mappers.UserMapper;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;

@CommandClass @AllArgsConstructor
public class General {
  private final UserMapper userMapper;

  @Command @Help(description = "Bui will send the birth day of said user.")
  public @NotNull String birthday(final @Args String @NotNull[] args, final @Event @NotNull MessageReceivedEvent e) {
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
}