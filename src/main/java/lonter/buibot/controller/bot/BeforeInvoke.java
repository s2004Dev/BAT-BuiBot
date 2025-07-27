package lonter.buibot.controller.bot;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.buibot.controller.commands.functions.XPManager;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component @AllArgsConstructor
public final class BeforeInvoke {
  private static final Logger log = LoggerFactory.getLogger(BeforeInvoke.class);

  private final SharedResources shared;
  private final XPManager xpManager;

  public void logic(final @NotNull MessageReceivedEvent e) {
    val id = e.getAuthor().getIdLong();
    val lvlThen = xpManager.getLevel(id);

    xpManager.addXP(id);

    val lvlNow = xpManager.getLevel(id);

    if(lvlThen == lvlNow)
      return;

    if(shared.outputChannel == null) {
      log.warn("logic(): outputChannel id is null.");
      System.exit(-1);
    }

    val outputChannel = shared.mainGuild.getTextChannelById(shared.outputChannel);

    if(outputChannel == null) {
      log.warn("logic(): output channel is null.");
      return;
    }

    outputChannel.sendMessage("Congratulations **" + e.getAuthor().getName() +
      "**! You just advanced to level **" + lvlNow + "**!").queue();
  }
}