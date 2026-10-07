package lonter.buibot.controller.bot;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.bat.batobjs.BatMRE;
import lonter.buibot.controller.commands.functions.XPManager;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component @AllArgsConstructor
public final class BeforeInvoke {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final SharedResources shared;
  private final XPManager xpManager;

  public void logic(final @NotNull BatMRE e) {
    val author = e.author;
    val id = author.id;
    val lvlThen = xpManager.getLevel(id);

    xpManager.addXP(id);

    val lvlNow = xpManager.getLevel(id);

    if(lvlThen == lvlNow)
      return;

    val source = e.source;

    val outputChannel = shared.getServer(source)
      .getChannelById(Long.parseLong(shared.getValue(source, "outputChannel")));

    if(outputChannel == null) {
      log.warn("logic(): output channel is null.");
      return;
    }

    outputChannel.sendMessage("Congratulations **" + author.localName +
      "**! You just advanced to level **" + lvlNow + "**!");
  }
}