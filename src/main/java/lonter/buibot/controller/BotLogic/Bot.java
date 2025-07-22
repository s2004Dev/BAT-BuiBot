package lonter.buibot.controller.BotLogic;

import lombok.val;

import lonter.bat.CommandHandler;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public final class Bot {
  @Value("${app.token:#{null}}")
  private String token;

  @Value("${app.mainGuild:#{null}}")
  private Long mainGuildId;

  private final CommandHandler handler;
  private final BeforeInvoke before;
  private final AfterInvoke after;
  private final SharedResources shared;

  private Bot(final @NotNull CommandHandler handler, final @NotNull BeforeInvoke before,
              final @NotNull AfterInvoke after, final @NotNull SharedResources shared) {
    this.handler = handler;
    this.before = before;
    this.after = after;
    this.shared = shared;
  }

  @EventListener(ApplicationReadyEvent.class)
  private void start() {
    if(token == null) {
      System.out.println("token is null.");
      System.exit(-1);
    }

    val builder = DefaultShardManagerBuilder.createDefault(token);

    builder.setStatus(OnlineStatus.ONLINE);

    try {
      builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS);
      builder.setMemberCachePolicy(MemberCachePolicy.ALL);
      builder.setChunkingFilter(ChunkingFilter.ALL);

      shared.shardManager = builder.build();
      shared.shardManager.addEventListener(new BotListener(handler, before, after, shared, mainGuildId));

      while(shared.mainGuild == null)
        Thread.onSpinWait();
    }

    catch(final @NotNull Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}