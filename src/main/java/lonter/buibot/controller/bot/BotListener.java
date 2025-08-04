package lonter.buibot.controller.bot;

import static lonter.buibot.controller.commands.Util.send;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.bat.CommandHandler;

import lonter.buibot.model.mappers.UserMapper;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component @AllArgsConstructor
public final class BotListener extends ListenerAdapter {
  private static final Logger log = LoggerFactory.getLogger(BotListener.class);

  private final CommandHandler handler;
  private final BeforeInvoke before;
  private final AfterInvoke after;
  private final SharedResources shared;
  private final UserMapper userMapper;

  @Override
  public void onMessageReceived(final @NotNull MessageReceivedEvent e) {
    val message = e.getMessage();

    if(message.getType() == MessageType.CHANNEL_PINNED_ADD) {
      message.delete().queue();
      return;
    }

    val author = e.getAuthor();

    if(author.isBot())
      return;

    if(!e.isFromGuild()) {
      send("Bui! You cannot interact with me outside of the server!", e);
      return;
    }

    try {
      before.logic(e);
      handler.invoke(e);
      after.logic(e);
    }

    catch(final @NotNull Exception ex) {
      ex.printStackTrace();

      log.warn("onMessageReceived(): author: {}", author.getName());
      log.warn("onMessageReceived(): message: {}", message.getContentRaw());

      if(!e.isFromGuild())
        return;

      val channel = e.getChannel();

      log.warn("onMessageReceived(): channel: {}; id: {}", channel.getName(), channel.getId());
      log.warn("onMessageReceived(): guild: {}", e.getGuild().getName());
    }
  }

  @Override
  public void onGuildReady(final @NotNull GuildReadyEvent e) {
    if(shared.mainGuildId == null) {
      log.warn("onGuildReady(): mainGuildId is null.");
      System.exit(-1);
    }

    shared.mainGuild = shared.shardManager.getGuildById(shared.mainGuildId);

    if(shared.mainGuild != null)
      return;

    log.warn("onGuildReady(): main guild is null.");
    System.exit(-1);
  }

  @Override
  public void onMessageReactionAdd(final @NotNull MessageReactionAddEvent e) {
    reactionLogic(e, true);
  }

  @Override
  public void onMessageReactionRemove(final @NotNull MessageReactionRemoveEvent e) {
    reactionLogic(e, false);
  }

  private void reactionLogic(final @NotNull GenericMessageReactionEvent e, final boolean add) {
    if(shared.roleplayChannel == null) {
      log.warn("reactionLogic() - {}: roleplay is null.", add);
      System.exit(-1);
    }

    if(e.getMessageIdLong() != shared.roleplayChannel || !e.getEmoji().getName().equals("⭐"))
      return;

    val member = e.getMember();

    if(shared.roleplayRole == null) {
      log.warn("reactionLogic() - {}: roleplayRole id is null.", add);
      System.exit(-1);
    }

    val roleplay = shared.mainGuild.getRoleById(shared.roleplayRole);

    if(member == null) {
      log.warn("reactionLogic() - {}: member is null.", add);
      return;
    }

    val roles = member.getRoles().contains(roleplay);

    if(add == roles || roleplay == null)
      return;

    (add ? shared.mainGuild.addRoleToMember(member, roleplay) :
      shared.mainGuild.removeRoleFromMember(member, roleplay)).queue();
  }

  @Override
  public void onGuildMemberJoin(final @NotNull GuildMemberJoinEvent e) {
    if(e.getUser().isBot())
      return;

    val member = e.getMember();

    if(shared.unverified == null) {
      log.warn("onGuildMemberJoin(): unverified id is null.");
      System.exit(-1);
    }

    val unverified = shared.mainGuild.getRoleById(shared.unverified);
    val id = member.getIdLong();

    if(userMapper.exists(id))
      userMapper.update(id, "here", true);

    else
      userMapper.insert(member.getIdLong());

    if(member.getRoles().contains(unverified))
      return;

    if(unverified == null) {
      log.warn("onGuildMemberJoin(): unverified role is null.");
      return;
    }

    shared.mainGuild.addRoleToMember(member, unverified).queue();

    if(shared.staff == null) {
      log.warn("onGuildMemberJoin(): staff id is null.");
      System.exit(-1);
    }

    val channel = shared.mainGuild.getTextChannelById(shared.staff);

    if(channel == null) {
      log.warn("onGuildMemberJoin(): Staff channel is null.");
      return;
    }

    channel.sendMessage(member.getAsMention() + " joined.").queue();
  }

  @Override
  public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent e) {
    if(e.getUser().isBot())
      return;

    val member = e.getMember();

    if(member == null) {
      log.warn("onGuildMemberRemove(): member is null.");
      return;
    }

    userMapper.update(member.getIdLong(), "here", false);

    if(shared.unverified == null) {
      log.warn("onGuildMemberRemove(): unverified id is null.");
      System.exit(-1);
    }

    val unverified = shared.mainGuild.getRoleById(shared.unverified);

    if(unverified == null) {
      log.warn("onGuildMemberRemove(): unverified role is null.");
      return;
    }

    if(member.getRoles().contains(unverified)) {
      if(shared.staff == null) {
        log.warn("onGuildMemberRemove(): staff id is null.");
        System.exit(-1);
      }

      val channel = shared.mainGuild.getTextChannelById(shared.staff);

      if(channel == null) {
        log.warn("onGuildMemberRemove(): staff channel is null.");
        return;
      }

      channel.sendMessage(member.getAsMention() + "(" + member.getEffectiveName() + ") left.").queue();

      return;
    }

    if(shared.mainChannel == null) {
      log.warn("onGuildMemberRemove(): mainChannel id is null.");
      System.exit(-1);
    }

    val general = shared.mainGuild.getTextChannelById(shared.mainChannel);

    if(general == null) {
      log.warn("onGuildMemberRemove(): Main channel is null.");
      return;
    }

    general.sendMessage(member.getAsMention() + "(" + member.getEffectiveName() + ") left the valley...").queue();
  }

  @Override
  public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent e) {
    val member = e.getMember();
    val roles = e.getRoles();

    if(shared.kohai == null) {
      log.warn("onGuildMemberRoleAdd(): kohai role is null.");
      System.exit(-1);
    }

    val kohai = shared.mainGuild.getRoleById(shared.kohai);

    if(!roles.contains(kohai))
      return;

    if(shared.mainChannel == null) {
      log.warn("onGuildMemberRoleAdd(): mainChannel id is null.");
      System.exit(-1);
    }

    val general = shared.mainGuild.getTextChannelById(shared.mainChannel);

    if(general == null) {
      log.warn("onGuildMemberRoleAdd(): Main channel is null.");
      return;
    }

    general.sendMessage("Bui! Welcome " + member.getAsMention() + "! Remember to keep an eye on " +
      "<#1051122051466936340> and, if you want, you can introduce yourself at <#1046927743188729876>, have a nice " +
      "stay! <:Star:1100387219975442502>").queue();
  }
}