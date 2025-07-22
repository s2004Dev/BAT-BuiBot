package lonter.buibot.controller.BotLogic;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.buibot.controller.commands.XPManager;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component @AllArgsConstructor
public final class BeforeInvoke {
  private final SharedResources shared;
  private final XPManager xpManager;

  public void logic(final @NotNull MessageReceivedEvent e) {
    val id = e.getAuthor().getIdLong();
    val lvlThen = xpManager.getLevel(id);

    xpManager.addXP(id);

    val lvlNow = xpManager.getLevel(id);

    if(lvlThen == lvlNow)
      return;

    shared.getOutputChannel().sendMessage("Congratulations **" + e.getAuthor().getName() +
      "**! You just advanced to level **" + lvlNow + "**!").queue();
  }
}