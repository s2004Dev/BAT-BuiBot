package lonter.buibot.model.mappers;

import lonter.buibot.model.entities.User;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

@Mapper
public interface UserMapper {
  @Select("SELECT id, bui, buizel FROM users WHERE id = #{ id }")
  @NotNull Optional<User> findById(final long id);

  @Select("SELECT xps FROM users WHERE id = #{ id }")
  @NotNull Optional<Integer> findXpsById(final long id);

//  @Select("SELECT joined FROM users WHERE id = #{ id }")
//  @NotNull Optional<Integer> findJoinedById(final long id);

  @Select("SELECT birthday, timezone FROM users WHERE id = #{ id }")
  @NotNull Optional<User> findBirthdayById(final long id);

  @Select("SELECT EXISTS (SELECT 1 FROM users WHERE id = #{ id })")
  boolean exists(final long id);

  @Select("INSERT INTO users(id, bui, buizel, xps, joined, here) " +
          "VALUES(#{ id }, NULL, NULL, 0, CURRENT_TIMESTAMP, TRUE) RETURNING *")
  @NotNull User insert(final long id);

  @Update("UPDATE users SET ${ category } = #{ value } WHERE id = #{ id }")
  void update(final long id, final @NotNull String category, final Object value);

  @Update("UPDATE users SET bui = #{ bui }, buizel = #{ buizel } WHERE id = #{ id }")
  void updateBuis(final @NotNull User user);

  @Update("UPDATE users SET birthday = #{ birthday }, timezone = #{ timezone } WHERE id = #{ id }")
  void updateBirth(final @NotNull User user);

  @Select("SELECT id, ${ category } FROM users WHERE ${ category } IS NOT NULL AND here = TRUE " +
          "ORDER BY ${ category } DESC LIMIT 10")
  @NotNull ArrayList<User> findAllForRank(final @NotNull String category);

  @Select("SELECT id, birthday, timezone FROM users WHERE birthday IS NOT NULL AND here = TRUE")
  @NotNull ArrayList<User> findAllForBirth();

  @Select("SELECT COUNT(*) FROM users WHERE xps IS NOT NULL AND here = TRUE")
  @NotNull Integer getCount();

  @Select("SELECT ROW_NUMBER() OVER (ORDER BY xps) FROM users WHERE id = #{ id }")
  @NotNull Integer getIndex(final long id);
}