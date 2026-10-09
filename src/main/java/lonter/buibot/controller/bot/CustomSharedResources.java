package lonter.buibot.controller.bot;

import lonter.bat.SharedResources;
import lonter.buibot.model.entities.ReactionRole;
import lonter.buibot.model.mappers.ReactionRoleMapper;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public final class CustomSharedResources extends SharedResources {
  public ArrayList<ReactionRole> reactionRoles = new ArrayList<>();

  @Value("${app.coordsAPI:#{null}}")
  public String coorsAPI;

  @Value("${app.timezoneAPI:#{null}}")
  public String timezoneAPI;

  private final ReactionRoleMapper rrMapper;

  @Autowired
  public CustomSharedResources(final @NotNull ReactionRoleMapper rrMapper, final @NotNull Environment env) {
    super(env);
    this.rrMapper = rrMapper;
  }

  public void updateReactionRoles() {
    reactionRoles = rrMapper.findAll();
  }
}