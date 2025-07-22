package lonter.buibot.controller.BotLogic;

import static lonter.buibot.controller.commands.Util.*;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.buibot.model.mappers.UserMapper;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component @AllArgsConstructor
public class AfterInvoke {
  private final UserMapper userMapper;

  public void logic(final @NotNull MessageReceivedEvent e) {
    val msgRaw = e.getMessage().getContentRaw().toLowerCase().trim();

    if(msgRaw.contains(":3"))
      colonThree(e);

    if(!msgRaw.contains("bui"))
      return;

    bui(e);

    val id = e.getAuthor().getIdLong();
    val found = userMapper.findById(id);
    val user = found.orElseGet(( ) -> userMapper.insert(id));

    var bui = countOccurrences(msgRaw, "bui");
    var buizel = countOccurrences(msgRaw, "buizel");

    if(bui > 5)
      bui = 5;

    if(buizel > 5)
      buizel = 5;

    user.setBui(user.getBui()+bui);
    user.setBuizel(user.getBuizel()+buizel);

    userMapper.updateBuis(user);
  }

  private void bui(final @NotNull MessageReceivedEvent e) {
    if(rand(0, 20).equals(rand(0, 20)))
      send("Bui!", e);
  }

  private void colonThree(final @NotNull MessageReceivedEvent e) {
    if(rand(0, 30).equals(rand(0, 30)))
      send(":3", e);
  }
}
