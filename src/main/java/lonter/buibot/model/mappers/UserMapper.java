package lonter.buibot.model.mappers;

import lonter.buibot.model.entities.User;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Mapper
public interface UserMapper {
  @Select("SELECT id, bui, buizel FROM users WHERE id = #{ id }")
  @NotNull Optional<User> findById(final long id);

  @Select("SELECT xps FROM users WHERE id = #{ id }")
  @NotNull Optional<Integer> findXpsById(final long id);

//  @Select("SELECT joined FROM users WHERE id = #{ id }")
//  @NotNull Optional<Integer> findJoinedById(final long id);

  @Select("SELECT birthday FROM users WHERE id = #{ id }")
  @NotNull Optional<Integer> findBirthdayById(final long id);

//  @Select("SELECT EXISTS (SELECT 1 FROM users WHERE id = #{ id })")
//  boolean exists(final long id);

  @Insert("INSERT INTO users(id, bui, buizel, xps, joined) VALUES(#{ id }, NULL, NULL, 0, CURRENT_TIMESTAMP)")
  @NotNull User insert(final long id);

  @Update("UPDATE users SET xps = #{ xps } WHERE id = #{ id }")
  void updateXps(final long id, final int xps);

  @Update("UPDATE users SET bui = #{ bui }, buizel = #{ buizel } WHERE id = #{ id }")
  void updateBuis(final @NotNull User user);
}