package lonter.buibot.controller.BotLogic;

import lombok.val;

import lonter.bat.CommandHandler;

import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BotListener extends ListenerAdapter {
  private final CommandHandler handler;
  private final BeforeInvoke before;
  private final AfterInvoke after;
  private final SharedResources shared;
  private final Long mainGuildId;

  public BotListener(final @NotNull CommandHandler handler, final @NotNull BeforeInvoke before,
                     final @NotNull AfterInvoke after, final @NotNull SharedResources shared,
                     final @Nullable Long mainGuildId) {
    this.handler = handler;
    this.before = before;
    this.after = after;
    this.shared = shared;
    this.mainGuildId = mainGuildId;
  }

  @Override
  public void onMessageReceived(final @NotNull MessageReceivedEvent e) {
    val message = e.getMessage();

    if(message.getType() == MessageType.CHANNEL_PINNED_ADD) {
      message.delete().queue();
      return;
    }

    val author = e.getAuthor();

    if(author.isBot())
      return;

    try {
      before.logic(e);
      handler.invoke(e);
      after.logic(e);
    }

    catch(final @NotNull Exception ex) {
      ex.printStackTrace();

      System.out.println("Author: " + author.getName());
      System.out.println("Message: " + message.getContentRaw());

      if(!e.isFromGuild())
        return;

      val channel = e.getChannel();

      System.out.println("Channel: " + channel.getName() + "; id: " + channel.getId());
      System.out.println("Guild: " + e.getGuild().getName());
    }
  }

  @Override
  public void onGuildReady(@NotNull GuildReadyEvent e) {
    if(mainGuildId == null) {
      System.out.println("mainGuildId is null.");
      System.exit(-1);
    }

    shared.mainGuild = shared.shardManager.getGuildById(mainGuildId);

    if(shared.mainGuild != null)
      return;

    System.out.println("Main Guild is null.");
    System.exit(-1);
  }
}