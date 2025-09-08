package lonter.buibot.model.mappers;

import lonter.buibot.model.entities.ReactionRole;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Mapper
public interface ReactionRoleMapper {
  @Select("SELECT id, name, message_id AS messageId, role_id AS roleId, emoji_id AS emojiId FROM reaction_roles")
  @NotNull ArrayList<ReactionRole> findAll();

  @Select("INSERT INTO reaction_roles(id, name, message_id, role_id, emoji_id) " +
          "VALUES(nextval('reaction_role_ids'), #{ name }, #{ messageId }, #{ roleId }, #{ emojiId })")
  void insert(final @NotNull ReactionRole __);

  @Delete("DELETE FROM reaction_roles WHERE id = #{ id }")
  void deleteById(final long id);
}