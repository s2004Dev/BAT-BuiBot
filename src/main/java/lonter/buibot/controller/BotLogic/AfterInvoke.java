package lonter.buibot.controller.BotLogic;

import static lonter.buibot.controller.commands.Util.*;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.buibot.model.mappers.UserMapper;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@Component @AllArgsConstructor
public final class AfterInvoke {
  private final UserMapper userMapper;

  private static final HashMap<String, String> EMOJIS = new HashMap<>() {{
    put("dripping", "<a:Dripping:1117137900744736859>");
    put("hi", "<a:Hi:1056243750298468412>");
    put("lick", "<a:Lick:1100389591883731004>");
    put("okand", "<a:OkAnd:1045753381521727588>");
  }};

  public void logic(final @NotNull MessageReceivedEvent e) {
    val msgRaw = e.getMessage().getContentRaw().toLowerCase().trim();

    if(countOccurrences(msgRaw, ":") > 1)
      emoji(msgRaw, e);

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

  private static void emoji(@NotNull String input, final @NotNull MessageReceivedEvent e) {
    for(val em: EMOJIS.values())
      input = input.replace(em, "");

    val msg = new StringBuilder();

    for(var em: findEmojis(input)) {
      em = em.toLowerCase();

      if(EMOJIS.containsKey(em))
        msg.append(EMOJIS.get(em)).append(" ");
    }

    if(msg.isEmpty())
      return;

    e.getMessage().reply(msg.toString()).mentionRepliedUser(false).queue();
  }

  private static String @NotNull[] findEmojis(final @NotNull String input) {
    val emojis = new ArrayList<String>();
    var isEmoji = false;
    var index = 0;

    for(var i = 0; i < input.length(); i++) {
      if(input.charAt(i) != ':')
        continue;

      if(isEmoji) {
        emojis.add(input.substring(index, i));
        isEmoji = false;

        continue;
      }

      index = i+1;
      isEmoji = true;
    }

    return emojis.toArray(new String[0]);
  }
}
