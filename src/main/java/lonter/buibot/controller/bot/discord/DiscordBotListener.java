package lonter.buibot.controller.bot.discord;

import lombok.AllArgsConstructor;

import lonter.bat.wrappers.discord.DiscordGGE;
import lonter.bat.wrappers.discord.DiscordGRE;
import lonter.bat.wrappers.discord.DiscordMRE;
import lonter.bat.wrappers.discord.DiscordRCE;
import lonter.buibot.controller.bot.BotListener;
import lonter.buibot.controller.bot.SharedResources;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component @AllArgsConstructor
public final class DiscordBotListener extends ListenerAdapter {
  private static final String source = "discord";

  private final SharedResources shared;
  private final BotListener botListener;

  @Override public void onMessageReceived(final @NotNull MessageReceivedEvent e) {
    botListener.onMessageReceived(new DiscordMRE(e));
  }

  @Override public void onGuildReady(final @NotNull GuildReadyEvent __) {
    botListener.onGuildReady(source);
  }

  @Override public void onMessageReactionAdd(final @NotNull MessageReactionAddEvent e) {
    shared.reactionRoles.forEach(rr -> botListener.reactionLogic(new DiscordGRE(e), rr));
  }

  @Override public void onMessageReactionRemove(final @NotNull MessageReactionRemoveEvent e) {
    shared.reactionRoles.forEach(rr -> botListener.reactionLogic(new DiscordGRE(e), rr));
  }

  @Override public void onGuildMemberJoin(final @NotNull GuildMemberJoinEvent e) {
    botListener.onMemberJoinLeave(new DiscordGGE(e));
  }

  @Override public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent e) {
    botListener.onMemberJoinLeave(new DiscordGGE(e));
  }

  @Override public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent e) {
    botListener.onGuildMemberRoleAdd(new DiscordRCE(e));
  }
}