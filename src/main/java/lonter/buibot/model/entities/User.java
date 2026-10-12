package lonter.buibot.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.ZoneId;

@Data @AllArgsConstructor
@NoArgsConstructor @Builder
public class User {
  public long id;
  public String idSource;
  public String source;
  public int bui;
  public int buizel;
  public int xps;
  public LocalDateTime joined;
  public MonthDay birthday;
  public ZoneId timezone;
  public boolean here;

  public void merge(final @NotNull User from) {
    bui += from.bui;
    buizel += from.buizel;
    xps += from.xps;

    if(joined.isAfter(from.joined))
      joined = from.joined;

    if(birthday == null)
      birthday = from.birthday;

    if(timezone == null)
      timezone = from.timezone;
  }
}