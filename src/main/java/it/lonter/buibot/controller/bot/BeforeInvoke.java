package it.lonter.buibot.controller.bot;

import lombok.AllArgsConstructor;
import lombok.val;

import it.lonter.bat.batobjs.BatMRE;
import it.lonter.buibot.controller.commands.features.XPManager;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component @AllArgsConstructor
public final class BeforeInvoke {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final CustomSharedResources shared;
  private final XPManager xpManager;

  public void logic(final @NotNull BatMRE e) {
    val author = e.author;
    val source = e.source;
    val id = String.valueOf(author.id);
    val lvlThen = xpManager.getLevel(source, id);

    xpManager.addXP(source, id);

    val lvlNow = xpManager.getLevel(source, id);

    if(lvlThen == lvlNow)
      return;

    shared.getServer(source).getChannelById(Long.parseLong(shared.getValue(source, "outputChannel")))
        .orElseThrow(( ) -> {
      log.warn("logic(): output channel is null.");
      System.exit(-1);
      return null;
    }).sendMessage("Congratulations **" + author.localName + "**! You just advanced to level **" + lvlNow + "**!");
  }
}