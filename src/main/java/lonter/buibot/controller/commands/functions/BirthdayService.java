package lonter.buibot.controller.commands.functions;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.buibot.controller.bot.CustomSharedResources;
import lonter.buibot.model.entities.User;
import lonter.buibot.model.mappers.UserMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service @AllArgsConstructor
public final class BirthdayService {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final UserMapper repo;
  private final RestTemplate restTemplate;
  private final CustomSharedResources shared;

  public void setBirthday(final long idB, final int day, final int month, final @NotNull String locationName,
                          final @NotNull String sourceB) throws Exception {
    val foundZoneId = findTimezoneForLocation(locationName);

    if(foundZoneId.isEmpty())
      throw new InvalidCityException("Bui! I can't find the timezone for \"" + locationName + "\"...");

    repo.updateBirth(new User() {{
      idSource = String.valueOf(idB);
      source = sourceB;
      birthday = MonthDay.of(month, day);
      timezone = foundZoneId.get();
    }});
  }

  private @NotNull Optional<ZoneId> findTimezoneForLocation(final @NotNull String locationName) {
    if(shared.coorsAPI == null) {
      log.warn("findTimezoneForLocation(): coordsAPI is null.");
      System.exit(-1);
    }

    val cordsList = restTemplate.getForObject(UriComponentsBuilder.fromUriString(shared.coorsAPI)
      .queryParam("q", locationName).queryParam("format", "json").queryParam("limit", 1).toUriString(),
      JsonNode.class);

    if(cordsList == null || cordsList.isEmpty())
      return Optional.empty();

    if(shared.timezoneAPI == null) {
      log.warn("findTimezoneForLocation(): timezoneAPI is null.");
      System.exit(-1);
    }

    val cords = cordsList.get(0);

    val timezone = restTemplate.getForObject(UriComponentsBuilder.fromUriString(shared.timezoneAPI)
      .queryParam("format", "json").queryParam("key", "OCEDTNLOHIOX").queryParam("by", "position")
      .queryParam("fields", "zoneName").queryParam("lat", cords.get("lat").asText()).queryParam("lng",
        cords.get("lon").asText()).toUriString(), JsonNode.class);

    if(timezone != null && "OK".equals(timezone.get("status").asText()) && timezone.has("zoneName"))
      return Optional.of(ZoneId.of(timezone.get("zoneName").asText()));

    return Optional.empty();
  }

  @Scheduled(cron = "0 0 * * * *")
  public void checkBirthdays() {
    val users = repo.findAllForBirth();

    for(val user: users) {
      val source = user.source;

      if(user.birthday == null || user.timezone == null) {
        log.warn("checkBirthdays() - {}: {} has null birthday information.", source, user.id);
        continue;
      }

      if(!user.birthday.equals(MonthDay.now(user.timezone)) || ZonedDateTime.now(user.timezone).getHour() != 0)
        continue;

      val news = shared.getServer(source).getChannelById(Long.parseLong(shared.getValue(source, "news")));

      if(news == null) {
        log.warn("checkBirthdays() - {}: news channel is null.", source);
        return;
      }

      news.sendMessage("Happy birthday <@" + user.idSource + ">!");
    }
  }
}