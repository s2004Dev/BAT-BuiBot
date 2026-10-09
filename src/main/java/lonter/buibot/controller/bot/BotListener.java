package lonter.buibot.controller.bot;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.bat.CommandHandler;
import lonter.bat.batobjs.*;
import lonter.buibot.model.entities.ReactionRole;
import lonter.buibot.model.mappers.UserMapper;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component @AllArgsConstructor
public final class BotListener extends BatListenerAdapter {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final CustomSharedResources shared;
  private final UserMapper userMapper;

  private final BeforeInvoke before;
  private final CommandHandler handler;
  private final AfterInvoke after;

  @Override public void onMessageReceived(final @NotNull BatMRE e) {
    val message = e.message;

    if(message.isSystemPinned()) {
      message.delete();
      return;
    }

    val author = e.author;

    if(author.isBot())
      return;

    try {
      before.logic(e);
      handler.invoke(e);
      after.logic(e);
    }

    catch(final @NotNull Exception ex) {
      log.error("BatMRE {} threw an exception: ", e.source, ex);

      log.warn("onMessageReceived(): author: {}", author.globalName);
      log.warn("onMessageReceived(): message: {}", message.text);

      if(e.server == null)
        return;

      val channel = e.channel;

      log.warn("onMessageReceived(): channel: {}; id: {}", channel.name, channel.id);
      log.warn("onMessageReceived(): guild: {}", e.server.name);
    }
  }

  @Override public void onServerReady(final @NotNull String source) {
    val server = shared.getShard(source).getServerById(Long.parseLong(shared.getValue(source, "mainServer")));

    if(server == null) {
      log.warn("onServerReady(): {} main server is null.", source);
      System.exit(-1);
    }

    shared.setServer(source, server);
    shared.updateReactionRoles();
  }

  @Override public void onMessageReaction(final @NotNull BatGRE e) {
    shared.reactionRoles.forEach(rr -> reactionLogic(e, rr));
  }

  @Override public void onMemberJoinLeave(final @NotNull BatGGE e) {
    val author = e.author;

    if(author.isBot())
      return;

    val source = e.source;
    val type = e.eventType;
    val server = shared.getServer(source);
    val unverified = server.getRoleById(Long.parseLong(shared.getValue(source, "unverified")));

    if(unverified == null) {
      log.warn("onMemberJoinLeave() - {}, {}: unverified role is null.", source, type);
      System.exit(-1);
    }

    val staff = server.getChannelById(Long.parseLong(shared.getValue(e.source, "staff")));

    if(staff == null) {
      log.warn("onMemberJoinLeave() - {}, {}: Staff channel is null.", source, type);
      System.exit(-1);
    }

    val id = author.id;
    val hasUnverified = author.hasRole(unverified);
    val asMention = author.asMention;

    if(e.eventType.equals("join")) {
      if(userMapper.exists(id))
        userMapper.update(id, "here", true);

      else
        userMapper.insert(id);

      if(hasUnverified)
        return;

      server.addRoleToMember(author, unverified);

      staff.sendMessage(asMention + " joined.");

      return;
    }

    userMapper.update(id, "here", false);

    val localName = author.localName;

    if(hasUnverified) {
      staff.sendMessage(asMention + "(" + localName + ") left.");
      return;
    }

    val general = server.getChannelById(Long.parseLong(shared.getValue(source, "general")));

    if(general == null) {
      log.warn("onMemberJoinLeave() - {}, {}: Main channel is null.", source, type);
      return;
    }

    general.sendMessage(asMention + "(" + localName + ") left the valley...");
  }

  @Override public void onServerMemberRoleChange(final @NotNull BatRCE e) {
    if(!e.eventType.equals("add"))
      return;

    val source = e.source;
    val kohai = Long.parseLong(shared.getValue(source, "kohai"));

    if(e.roles.stream().noneMatch(i -> i.id == kohai))
      return;

    val general = shared.getServer(source).getChannelById(Long.parseLong(shared.getValue(source, "mainChannel")));

    if(general == null) {
      log.warn("onGuildMemberRoleAdd() - {}: Main channel is null.", source);
      return;
    }

    general.sendMessage("Bui! Welcome " + e.author.asMention + "! Remember to keep an eye on <#" +
      shared.getValue(source, "news") + "> and, if you want, you can introduce yourself at <#" +
      shared.getValue(source, "introduction") + ">, have a nice stay! " + shared.getValue(source, "emoji"));
  }

  public void reactionLogic(final @NotNull BatGRE e, final @NotNull ReactionRole rr) {
    if(e.messageId != rr.messageId || !e.emojiId.equals(rr.emojiId))
      return;

    val source = e.source;
    val role = shared.getServer(source).getRoleById(rr.roleId);

    if(role == null) {
      log.warn("reactionLogic() - {}: role {} is null.", e.eventType, rr.roleId);
      return;
    }

    val author = e.author;
    val roles = author.hasRole(role);
    val add = e.eventType.equals("add");

    if(add == roles)
      return;

    val server = shared.getServer(source);

    if(add) {
      server.addRoleToMember(author, role);
      return;
    }

    server.removeRoleFromMember(author, role);
  }
}