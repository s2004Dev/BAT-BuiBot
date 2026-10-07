package lonter.buibot.controller.bot.fluxer;

import lombok.AllArgsConstructor;

import lonter.bat.wrappers.fluxer.FluxerGGE;
import lonter.bat.wrappers.fluxer.FluxerGRE;
import lonter.bat.wrappers.fluxer.FluxerMRE;
import lonter.bat.wrappers.fluxer.FluxerRCE;
import lonter.buibot.controller.bot.BotListener;
import lonter.buibot.controller.bot.SharedResources;
import lonter.jfa.api.events.guild.GuildReadyEvent;
import lonter.jfa.api.events.guild.member.GuildMemberJoinEvent;
import lonter.jfa.api.events.guild.member.GuildMemberRemoveEvent;
import lonter.jfa.api.events.guild.member.GuildMemberRoleAddEvent;
import lonter.jfa.api.events.message.MessageReceivedEvent;
import lonter.jfa.api.events.message.react.MessageReactionAddEvent;
import lonter.jfa.api.events.message.react.MessageReactionRemoveEvent;
import lonter.jfa.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component @AllArgsConstructor
public final class FluxerBotListener extends ListenerAdapter {
  private static final String source = "fluxer";

  private final SharedResources shared;
  private final BotListener botListener;

  @Override public void onMessageReceived(final @NotNull MessageReceivedEvent e) {
    botListener.onMessageReceived(new FluxerMRE(e));
  }

  @Override public void onGuildReady(final @NotNull GuildReadyEvent __) {
    botListener.onGuildReady(source);
  }

  @Override public void onMessageReactionAdd(final @NotNull MessageReactionAddEvent e) {
    shared.reactionRoles.forEach(rr -> botListener.reactionLogic(new FluxerGRE(e), rr));
  }

  @Override public void onMessageReactionRemove(final @NotNull MessageReactionRemoveEvent e) {
    shared.reactionRoles.forEach(rr -> botListener.reactionLogic(new FluxerGRE(e), rr));
  }

  @Override public void onGuildMemberJoin(final @NotNull GuildMemberJoinEvent e) {
    botListener.onMemberJoinLeave(new FluxerGGE(e));
  }

  @Override public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent e) {
    botListener.onMemberJoinLeave(new FluxerGGE(e));
  }

  @Override public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent e) {
    botListener.onGuildMemberRoleAdd(new FluxerRCE(e));
  }
}