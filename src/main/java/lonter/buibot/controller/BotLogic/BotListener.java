package lonter.buibot.controller.BotLogic;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.bat.CommandHandler;

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
  private final Long mainGuildId;

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
    if(mainGuildId == null) {
      System.out.println("mainGuildId is null.");
      System.exit(-1);
    }

    shared.mainGuild = shared.shardManager.getGuildById(mainGuildId);

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
    if(e.getMessageIdLong() != 1067801438102761567L || !e.getEmoji().getName().equals("⭐"))
      return;

    val member = e.getMember();
    val guild = e.getGuild();
    val role = guild.getRoleById(1060231459773886616L);

    if(member == null)
      return;

    val roles = member.getRoles().contains(role);

    if(add == roles || role == null)
      return;

    (add ? guild.addRoleToMember(member, role) : guild.removeRoleFromMember(member, role)).queue();
  }

  @Override
  public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent e) {
    val guild = e.getGuild();

    if(guild.getIdLong() != 1045727530390392952L)
      return;

    val member = e.getMember();
    val role = guild.getRoleById(1070415674851209236L);

    if(member.getRoles().contains(role))
      return;

    if(role == null)
      return;

    guild.addRoleToMember(member, role).queue();

    val channel = guild.getTextChannelById(1045735795828474027L);

    if(channel == null)
      return;

    channel.sendMessage(member.getAsMention() + " joined.").queue();
  }

  @Override
  public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent e) {
    val guild = e.getGuild();

    if(guild.getIdLong() != 1045727530390392952L)
      return;

    val member = e.getMember();

    if(member == null)
      return;

    val unverified = guild.getRoleById(1070415674851209236L);

    if(unverified == null)
      return;

    if(member.getRoles().contains(unverified)) {
      val channel = guild.getTextChannelById(1045735795828474027L);

      if(channel == null)
        return;

      channel.sendMessage(member.getAsMention() + "(" + member.getEffectiveName() + ") left.").queue();

      return;
    }

    val general = guild.getTextChannelById(1045727531766132848L);

    if(general == null)
      return;

    general.sendMessage(member.getAsMention() + "(" + member.getEffectiveName() + ") left the valley...").queue();
  }

  @Override
  public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent e) {
    val guild = e.getGuild();

    if(guild.getIdLong() != 1045727530390392952L)
      return;

    val member = e.getMember();
    val roles = e.getRoles();
    val role = guild.getRoleById(1045734718655692841L);

    if(!roles.contains(role))
      return;

    val general = guild.getTextChannelById(1045727531766132848L);

    if(general == null)
      return;

    general.sendMessage("Bui! Welcome " + member.getAsMention() + "! Remember to keep an eye on " +
      "<#1051122051466936340> and, if you want, you can introduce yourself at <#1046927743188729876>, have a nice " +
      "stay! <:Star:1100387219975442502>").queue();
  }
}