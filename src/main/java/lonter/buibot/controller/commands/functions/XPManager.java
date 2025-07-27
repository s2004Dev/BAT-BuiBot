package lonter.buibot.controller.commands.functions;

import static lonter.buibot.controller.commands.Util.rand;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.buibot.model.mappers.UserMapper;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;

@Service @AllArgsConstructor
public final class XPManager {
  public final HashMap<Long, Instant> cooldownMap = new HashMap<>();

  private final UserMapper userMapper;

  public void addXP(final long id) {
    val currentTime = Instant.now();
    val lastMessageTime = cooldownMap.getOrDefault(id, Instant.EPOCH);

    if(currentTime.isBefore(lastMessageTime.plusSeconds(60)))
      return;

    userMapper.update(id, "xps", getXP(id)+rand(15, 25));
    cooldownMap.put(id, currentTime);
  }

  public int getXP(final long id) {
    val found = userMapper.findXpsById(id);

    if(found.isPresent())
      return found.get();

    userMapper.insert(id);

    return 0;
  }

  public int getLevel(final long id) {
    val experience = getXP(id);

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