package lonter.buibot.controller.bot;

import lonter.buibot.model.entities.ReactionRole;
import lonter.buibot.model.mappers.ReactionRoleMapper;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public final class SharedResources {
  public ShardManager shardManager = null;
  public volatile Guild mainGuild = null;
  public ArrayList<ReactionRole> reactionRoles = new ArrayList<>();

  @Value("${app.outputChannel:#{null}}")
  public Long outputChannel;

  @Value("${app.token:#{null}}")
  public String token;

  @Value("${app.mainGuild:#{null}}")
  public Long mainGuildId;

  @Value("${app.kohai:#{null}}")
  public Long kohai;

  @Value("${app.mainChannel:#{null}}")
  public Long mainChannel;

  @Value("${app.unverified:#{null}}")
  public Long unverified;

  @Value("${app.staff:#{null}}")
  public Long staff;

  @Value("${app.news:#{null}}")
  public Long news;

  @Value("${app.coordsAPI:#{null}}")
  public String coorsAPI;

  @Value("${app.timezoneAPI:#{null}}")
  public String timezoneAPI;

  @Value("${app.prefix}")
  public String prefix;

  @Value("${app.owner:#{null}}")
  public Long owner;

  private final ReactionRoleMapper rrMapper;

  @Autowired
  public SharedResources(final @NotNull ReactionRoleMapper rrMapper) {
    this.rrMapper = rrMapper;
  }

  public void updateReactionRoles() {
    reactionRoles = rrMapper.findAll();
  }
}