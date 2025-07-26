package lonter.buibot.controller.bot;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.bat.CommandHandler;

import lonter.buibot.model.mappers.UserMapper;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service @AllArgsConstructor
public final class Bot {
  private final CommandHandler handler;
  private final BeforeInvoke before;
  private final AfterInvoke after;
  private final SharedResources shared;
  private final UserMapper userMapper;

  @EventListener(ApplicationReadyEvent.class)
  private void start() {
    if(shared.token == null) {
      System.out.println("token is null.");
      System.exit(-1);
    }

    val builder = DefaultShardManagerBuilder.createDefault(shared.token);

    builder.setStatus(OnlineStatus.IDLE);
    builder.setActivity(Activity.watching("Buizels"));

    try {
      builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS);
      builder.setMemberCachePolicy(MemberCachePolicy.ALL);
      builder.setChunkingFilter(ChunkingFilter.ALL);

      shared.shardManager = builder.build();
      shared.shardManager.addEventListener(new BotListener(handler, before, after, shared, userMapper));

      while(shared.mainGuild == null)
        Thread.onSpinWait();
    }

    catch(final @NotNull Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}