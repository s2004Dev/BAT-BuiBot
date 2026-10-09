package lonter.buibot.controller.bot.definition;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.bat.wrappers.fluxer.FluxerBotListener;
import lonter.bat.wrappers.fluxer.FluxerShard;
import lonter.buibot.controller.bot.CustomSharedResources;
import lonter.jfa.api.OnlineStatus;
import lonter.jfa.api.entities.Activity;
import lonter.jfa.api.requests.GatewayIntent;
import lonter.jfa.api.sharding.DefaultShardManagerBuilder;
import lonter.jfa.api.utils.ChunkingFilter;
import lonter.jfa.api.utils.MemberCachePolicy;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service @AllArgsConstructor
public final class FluxerBot {
  private static final String SOURCE = "fluxer";

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final FluxerBotListener listener;
  private final CustomSharedResources shared;

  @EventListener(ApplicationReadyEvent.class)
  private void start() {
    shared.initServer(SOURCE);

    try {
      val shard = DefaultShardManagerBuilder.createDefault(shared.getValue(SOURCE, "token"))
        .setStatus(OnlineStatus.IDLE).setActivity(Activity.watching("Buizels"))
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        .setMemberCachePolicy(MemberCachePolicy.ALL).setChunkingFilter(ChunkingFilter.ALL).build();

      shared.setShard(SOURCE, new FluxerShard(shard));
      shard.addEventListener(listener);
    }

    catch(final @NotNull Exception e) {
      log.error("{} application threw an exception: ", SOURCE, e);
      System.exit(-1);
    }
  }
}