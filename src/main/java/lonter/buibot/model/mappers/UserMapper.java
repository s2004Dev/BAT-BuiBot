package lonter.buibot.model.mappers;

import lonter.buibot.model.entities.User;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

@Mapper
public interface UserMapper {
  @Select("SELECT u.id, u.bui, u.buizel, u.xps, u.joined, u.birthday, u.timezone, u.source FROM users u " +
          "JOIN user_mappings m ON u.id = m.id WHERE m.id_source = #{ id } AND m.source = #{ source }")
  @NotNull Optional<User> findById(final @NotNull String source, final @NotNull String id);

  @Select("SELECT u.xps FROM users u JOIN user_mappings m ON u.id = m.id " +
          "WHERE m.id_source = #{ id } AND m.source = #{ source }")
  @NotNull Optional<Integer> findXpsById(final @NotNull String source, final @NotNull String id);

  @Select("SELECT u.birthday, u.timezone FROM users u JOIN user_mappings m ON u.id = m.id " +
          "WHERE m.id_source = #{ id } AND m.source = #{ source }")
  @NotNull Optional<User> findBirthdayById(final @NotNull String source, final @NotNull String id);

  @Select("SELECT EXISTS (SELECT 1 FROM users u JOIN user_mappings m ON u.id = m.id " +
          "WHERE m.id_source = #{ id } AND m.source = #{ source })")
  boolean exists(final @NotNull String source, final @NotNull String id);

  @Select("WITH new AS (INSERT INTO users(id, bui, buizel, xps, joined, here, source) " +
          "VALUES(nextval('user_ids'), NULL, NULL, 0, CURRENT_TIMESTAMP, TRUE, #{ source }) RETURNING * ), " +
          "mapping AS (INSERT INTO user_mappings(id, id_source, source) " +
          "SELECT id, #{ id }, #{ source } FROM new) SELECT * FROM new")
  @NotNull User insert(final @NotNull String source, final @NotNull String id);

  @Update("UPDATE users SET ${ category } = #{ value } " +
          "WHERE id = (SELECT id FROM user_mappings WHERE id_source = #{ id } AND source = #{ source })")
  void update(final @NotNull String source, final @NotNull String id, final @NotNull String category,
              final Object value);

  @Update("WITH updated_mapping AS (UPDATE user_mappings SET id = #{ user.id } " +
          "WHERE id = #{ oldId } AND source = #{ oldSource }) UPDATE users " +
          "SET bui = #{ user.bui }, buizel = #{ user.buizel }, xps = #{ user.xps }, joined = #{ user.joined }, " +
          "birthday = #{ user.birthday }, timezone = #{ user.timezone } WHERE id = #{ user.id }")
  void updateForMerge(final @NotNull User user, final long oldId, final @NotNull String oldSource);

  @Update("UPDATE users SET bui = #{ bui }, buizel = #{ buizel } WHERE id = #{ id }")
  void updateBuis(final @NotNull User user);

  @Update("UPDATE users SET birthday = #{ birthday }, timezone = #{ timezone } " +
          "WHERE id = (SELECT id FROM user_mappings WHERE id_source = #{ idSource } AND source = #{ source })")
  void updateBirth(final @NotNull User user);

  @Select("SELECT u.id, u.${ category }, m.id_source AS idSource FROM users u " +
          "JOIN user_mappings m ON u.id = m.id AND u.source = m.source " +
          "WHERE u.${ category } IS NOT NULL AND u.here = TRUE " +
          "ORDER BY u.${ category } DESC LIMIT 10")
  @NotNull ArrayList<User> findAllForRank(final @NotNull String category);

  @Select("SELECT u.id, u.birthday, u.timezone, m.id_source AS idSource FROM users u " +
          "JOIN user_mappings m ON u.id = m.id AND u.source = m.source " +
          "WHERE u.birthday IS NOT NULL AND u.here = TRUE")
  @NotNull ArrayList<User> findAllForBirth();

  @Select("SELECT COUNT(*) FROM users WHERE xps IS NOT NULL AND here = TRUE")
  @NotNull Integer getCount();

  @Select("SELECT res.rank FROM (SELECT id, ROW_NUMBER() OVER (ORDER BY xps DESC) AS rank FROM users) res " +
          "JOIN user_mappings m ON res.id = m.id WHERE m.id_source = #{ id } AND m.source = #{ source }")
  @NotNull Optional<Integer> getIndex(final @NotNull String source, final @NotNull String id);

  @Delete("DELETE FROM users WHERE id = #{ id }")
  void delete(final long id);
}