package lonter.buibot.controller.commands.functions;

import static lonter.buibot.controller.commands.Util.rand;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.buibot.model.mappers.UserMapper;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;

@Service @AllArgsConstructor
public final class XPManager {
  public final HashMap<String, Instant> cooldownMap = new HashMap<>();

  private final UserMapper repo;

  public void addXP(final @NotNull String source, final @NotNull String id) {
    val currentTime = Instant.now();
    val lastMessageTime = cooldownMap.getOrDefault(id, Instant.EPOCH);

    if(currentTime.isBefore(lastMessageTime.plusSeconds(60)))
      return;

    repo.update(source, id, "xps", getXP(source, id)+rand(15, 25));
    cooldownMap.put(id, currentTime);
  }

  public int getXP(final @NotNull String source, final @NotNull String id) {
    val found = repo.findXpsById(source, id);

    if(found.isPresent())
      return found.get();

    repo.insert(source, id);

    return 0;
  }

  public int getLevel(final @NotNull String source, final @NotNull String id) {
    val experience = getXP(source, id);

    if(experience < 100)
      return 0;

    var level = 0;
    var requiredExperience = 0;

    while(experience >= requiredExperience) {
      level++;
      requiredExperience += 5*level*level+50*level+100;
    }

    return level;
  }

  public int getXpFromLevel(final int level) {
    var requiredExperience = 0;

    for(int i = 0; i < level; i++)
      requiredExperience += 5*i*i+50*i+100;

    return requiredExperience;
  }
}