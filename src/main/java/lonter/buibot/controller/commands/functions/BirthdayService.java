package lonter.buibot.controller.commands.functions;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.val;

import lonter.buibot.controller.bot.SharedResources;
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
public class BirthdayService {
  private static final Logger log = LoggerFactory.getLogger(BirthdayService.class);

  private final UserMapper userMapper;
  private final RestTemplate restTemplate;
  private final SharedResources shared;

  public void setBirthday(final long id, final int day, final int month, final @NotNull String locationName)
      throws Exception {
    val foundZoneId = findTimezoneForLocation(locationName);

    if(foundZoneId.isEmpty())
      throw new InvalidCityException("Bui! I can't find the timezone for \"" + locationName + "\"...");

    val user = new User();

    user.id = id;
    user.birthday = MonthDay.of(month, day);
    user.timezone = foundZoneId.get();

    userMapper.updateBirth(user);
  }

  private @NotNull Optional<ZoneId> findTimezoneForLocation(final @NotNull String locationName) {
    val geoResponse = restTemplate.getForObject(UriComponentsBuilder
      .fromUriString("https://nominatim.openstreetmap.org/search").queryParam("q", locationName)
      .queryParam("format", "json").queryParam("limit", 1).toUriString(), JsonNode.class);

    if(geoResponse == null || geoResponse.isEmpty())
      return Optional.empty();

    val tzResponse = restTemplate.getForObject(UriComponentsBuilder.fromUriString("http://ip-api.com/json")
      .queryParam("lat", geoResponse.get(0).get("lat").asText()).queryParam("lon", geoResponse.get(0).get("lon")
        .asText()).queryParam("fields", "status,message,timezone").toUriString(), JsonNode.class);

    if(tzResponse != null && "success".equals(tzResponse.get("status").asText()) && tzResponse.has("timezone"))
      return Optional.of(ZoneId.of(tzResponse.get("timezone").asText()));

    return Optional.empty();
  }

  @Scheduled(cron = "0 0 * * * *")
  public void checkBirthdays() {
    val users = userMapper.findAllForBirth();

    if(shared.news == null) {
      log.warn("checkBirthdays(): news channel id is null.");
      return;
    }

    val news = shared.mainGuild.getTextChannelById(shared.news);

    if(news == null) {
      log.warn("checkBirthdays(): news channel is null.");
      return;
    }

    for(val user: users) {
      if(user.birthday == null || user.timezone == null) {
        log.warn("checkBirthdays(): {} has null birthday information.", user.id);
        continue;
      }

      if(user.birthday.equals(MonthDay.now(user.timezone)) && ZonedDateTime.now(user.timezone).getHour() == 0)
        news.sendMessage("Happy birthday <@" + user.getId() + ">!").queue();
    }
  }
}