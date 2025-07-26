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

@AllArgsConstructor
public final class BotListener extends ListenerAdapter {
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

      System.out.println("Author: " + author.getName());
      System.out.println("Message: " + message.getContentRaw());

      if(!e.isFromGuild())
        return;

      val channel = e.getChannel();

      System.out.println("Channel: " + channel.getName() + "; id: " + channel.getId());
      System.out.println("Guild: " + e.getGuild().getName());
    }
  }

  @Override
  public void onGuildReady(final @NotNull GuildReadyEvent e) {
    if(shared.mainGuildId == null) {
      System.out.println("mainGuildId is null.");
      System.exit(-1);
    }

    shared.mainGuild = shared.shardManager.getGuildById(shared.mainGuildId);

    if(shared.mainGuild != null)
      return;

    System.out.println("Main Guild is null.");
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
      System.out.println("roleplay is null.");
      return;
    }

    if(e.getMessageIdLong() != shared.roleplayChannel || !e.getEmoji().getName().equals("⭐"))
      return;

    val member = e.getMember();
    val roleplay = shared.mainGuild.getRoleById(shared.roleplayRole);

    if(member == null)
      return;

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
    val kohai = shared.mainGuild.getRoleById(shared.kohai);
    val id = member.getIdLong();

    if(userMapper.exists(id))
      userMapper.updateHere(id, true);

    else
      userMapper.insert(member.getIdLong());

    if(member.getRoles().contains(kohai))
      return;

    if(kohai == null) {
      System.out.println("Kohai role is null.");
      return;
    }

    shared.mainGuild.addRoleToMember(member, kohai).queue();

    val channel = shared.mainGuild.getTextChannelById(shared.mainChannel);

    if(channel == null) {
      System.out.println("onGuildMemberJoin(): Main channel is null.");
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
      System.out.println("Left member is null.");
      return;
    }

    userMapper.updateHere(member.getIdLong(), false);

    val unverified = shared.mainGuild.getRoleById(shared.unverified);

    if(unverified == null) {
      System.out.println("Unverified role is null.");
      return;
    }

    if(member.getRoles().contains(unverified)) {
      val channel = shared.mainGuild.getTextChannelById(shared.staff);

      if(channel == null) {
        System.out.println("Staff channel is null.");
        return;
      }

      channel.sendMessage(member.getAsMention() + "(" + member.getEffectiveName() + ") left.").queue();

      return;
    }

    val general = shared.mainGuild.getTextChannelById(shared.mainChannel);

    if(general == null) {
      System.out.println("onGuildMemberRemove(): Main channel is null.");
      return;
    }

    general.sendMessage(member.getAsMention() + "(" + member.getEffectiveName() + ") left the valley...").queue();
  }

  @Override
  public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent e) {
    val member = e.getMember();
    val roles = e.getRoles();
    val kohai = shared.mainGuild.getRoleById(shared.kohai);

    if(!roles.contains(kohai))
      return;

    val general = shared.mainGuild.getTextChannelById(shared.mainChannel);

    if(general == null) {
      System.out.println("onGuildMemberRoleAdd(): Main channel is null.");
      return;
    }

    general.sendMessage("Bui! Welcome " + member.getAsMention() + "! Remember to keep an eye on " +
      "<#1051122051466936340> and, if you want, you can introduce yourself at <#1046927743188729876>, have a nice " +
      "stay! <:Star:1100387219975442502>").queue();
  }
}