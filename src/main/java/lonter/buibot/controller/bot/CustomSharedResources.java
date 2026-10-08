package lonter.buibot.controller.bot;

import lombok.val;

import lonter.bat.SharedResources;
import lonter.bat.batobjs.BatServer;
import lonter.buibot.model.entities.ReactionRole;
import lonter.buibot.model.mappers.ReactionRoleMapper;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public final class CustomSharedResources extends SharedResources {
  public ArrayList<ReactionRole> reactionRoles = new ArrayList<>();

  @Value("${app.coordsAPI:#{null}}")
  public String coorsAPI;

  @Value("${app.timezoneAPI:#{null}}")
  public String timezoneAPI;

  private final HashMap<String, BatServer> servers = new HashMap<>();

  private final ReactionRoleMapper rrMapper;

  @Autowired public CustomSharedResources(final @NotNull ReactionRoleMapper rrMapper,
                                          final @NotNull Environment env) {
    super(env);
    this.rrMapper = rrMapper;
  }

  public void updateReactionRoles() {
    reactionRoles = rrMapper.findAll();
  }

  public void setServer(final @NotNull String source, final @NotNull BatServer server) {
    servers.put(source, server);
  }

  public @NotNull BatServer getServer(final @NotNull String source) {
    val server = servers.get(source);

    if(server == null)
      throw new IllegalStateException(source + " server is null");

    return server;
  }

  public boolean getReady(final @NotNull String source) {
    return servers.get(source) != null;
  }
}