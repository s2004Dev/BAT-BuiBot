package lonter.buibot.controller.bot.definition;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.bat.wrappers.discord.DiscordBotListener;
import lonter.bat.wrappers.discord.DiscordShard;
import lonter.buibot.controller.bot.CustomSharedResources;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service @AllArgsConstructor
public final class DiscordBot {
  private static final String source = "discord";

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final DiscordBotListener botListener;
  private final CustomSharedResources shared;

  @EventListener(ApplicationReadyEvent.class)
  private void start() {
    shared.updateReactionRoles();

    try {
      val shard = DefaultShardManagerBuilder.createDefault(shared.getValue(source, "token"))
        .setStatus(OnlineStatus.IDLE).setActivity(Activity.watching("Buizels"))
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        .setMemberCachePolicy(MemberCachePolicy.ALL).setChunkingFilter(ChunkingFilter.ALL).build();

      shared.setShard(source, new DiscordShard(shard));

      shard.addEventListener(botListener);

      while(!shared.getReady(source))
        Thread.onSpinWait();
    }

    catch(final @NotNull Exception e) {
      log.error("{} application threw an exception: ", source, e);
      System.exit(-1);
    }
  }
}