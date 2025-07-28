package lonter.buibot.controller.bot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public final class SharedResources {
  public ShardManager shardManager = null;
  public volatile Guild mainGuild = null;

  @Value("${app.outputChannel:#{null}}")
  public Long outputChannel;

  @Value("${app.token:#{null}}")
  public String token;

  @Value("${app.mainGuild:#{null}}")
  public Long mainGuildId;

  @Value("${app.roleplayChannel:#{null}}")
  public Long roleplayChannel;

  @Value("${app.roleplayRole:#{null}}")
  public Long roleplayRole;

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
}