package lonter.buibot.controller.bot;

import lombok.AllArgsConstructor;

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
public final class Bot {
  private static final Logger log = LoggerFactory.getLogger(Bot.class);

  private final BotListener botListener;
  private final SharedResources shared;

  @EventListener(ApplicationReadyEvent.class)
  private void start() {
    if(shared.token == null) {
      log.warn("start(): token is null.");
      System.exit(-1);
    }

    try {
      shared.shardManager = DefaultShardManagerBuilder.createDefault(shared.token).setStatus(OnlineStatus.IDLE)
        .setActivity(Activity.watching("Buizels")).enableIntents(GatewayIntent.MESSAGE_CONTENT,
          GatewayIntent.GUILD_MEMBERS).setMemberCachePolicy(MemberCachePolicy.ALL)
        .setChunkingFilter(ChunkingFilter.ALL).build();

      shared.shardManager.addEventListener(botListener);

      while(shared.mainGuild == null)
        Thread.onSpinWait();
    }

    catch(final @NotNull Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}