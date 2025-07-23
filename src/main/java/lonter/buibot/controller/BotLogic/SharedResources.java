package lonter.buibot.controller.BotLogic;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public final class SharedResources {
  public ShardManager shardManager = null;
  public volatile Guild mainGuild = null;

  private TextChannel outputChannel = null;

  @Value("${app.outputChannel:#{null}}")
  private Long outputChannelId;

  public @NotNull TextChannel getOutputChannel() {
    if(outputChannel == null) {
      if(mainGuild == null)
        throw new IllegalStateException("Bot has not been initialized yet, tried to get the output channel.");

      if(outputChannelId == null) {
        System.out.println("outputChannelId is null.");
        System.exit(-1);
      }

      outputChannel = mainGuild.getTextChannelById(outputChannelId);
    }

    if(outputChannel == null)
      throw new IllegalStateException("Output channel not found");

    return outputChannel;
  }
}