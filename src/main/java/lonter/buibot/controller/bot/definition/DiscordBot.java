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
  private static final String SOURCE = "discord";

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final DiscordBotListener listener;
  private final CustomSharedResources shared;

  @EventListener(ApplicationReadyEvent.class)
  private void start() {
    shared.initServer(SOURCE);

    try {
      val shard = DefaultShardManagerBuilder.createDefault(shared.getValue(SOURCE, "token"))
        .setStatus(OnlineStatus.IDLE).setActivity(Activity.watching("Buizels"))
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        .setMemberCachePolicy(MemberCachePolicy.ALL).setChunkingFilter(ChunkingFilter.ALL).build();

      shared.setShard(SOURCE, new DiscordShard(shard));
      shard.addEventListener(listener);
    }

    catch(final @NotNull Exception e) {
      log.error("{} application threw an exception: ", SOURCE, e);
      System.exit(-1);
    }
  }
}