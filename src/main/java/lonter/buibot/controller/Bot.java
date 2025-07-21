package lonter.buibot.controller;

import lombok.val;

import lonter.bat.CommandHandler;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public final class Bot {
  public ShardManager shardManager;

  private final String token;
  private final CommandHandler handler;

  private Bot(final @Value("${app.token}") @NotNull String token, final @NotNull CommandHandler handler) {
    this.token = token;
    this.handler = handler;
  }

  @EventListener(ApplicationReadyEvent.class)
  private void start() {
    val builder = DefaultShardManagerBuilder.createDefault(token);

    builder.setStatus(OnlineStatus.ONLINE);

    shardManager = null;

    try {
      builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS);
      builder.setMemberCachePolicy(MemberCachePolicy.ALL);
      builder.setChunkingFilter(ChunkingFilter.ALL);

      shardManager = builder.build();
      shardManager.addEventListener(new BotListener(handler));
    }

    catch(final @NotNull Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}