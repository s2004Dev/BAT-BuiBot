package lonter.buibot.controller;

import lombok.val;

import lonter.bat.CommandHandler;

import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

public final class BotListener extends ListenerAdapter {
  private final CommandHandler handler;

  public BotListener(final @NotNull CommandHandler handler) {
    this.handler = handler;
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
      handler.invoke(e);
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
}