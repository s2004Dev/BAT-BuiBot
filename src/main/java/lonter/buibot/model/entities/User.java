package lonter.buibot.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.MonthDay;
import java.time.ZoneId;
import java.util.Date;

@Data @AllArgsConstructor
@NoArgsConstructor @Builder
public final class User {
  public long id;
  public int bui;
  public int buizel;
  public int xps;
  public Date joined;
  public MonthDay birthday;
  public ZoneId timezone;
  public boolean here;
}