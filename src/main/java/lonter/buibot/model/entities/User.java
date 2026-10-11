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
public class User {
  public long id;
  public String idSource;
  public String source;
  public int bui;
  public int buizel;
  public int xps;
  public Date joined;
  public MonthDay birthday;
  public ZoneId timezone;
  public boolean here;
}