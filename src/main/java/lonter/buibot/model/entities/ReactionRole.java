package lonter.buibot.model.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.jetbrains.annotations.NotNull;

@Data @AllArgsConstructor
@NoArgsConstructor @Builder
public class ReactionRole {
  public long id;
  public String name;
  public long messageId;
  public long roleId;
  public String emojiId;

  public ReactionRole(final @NotNull String name, final long messageId, final long roleId,
                      final @NotNull String emojiId) {
    this.name = name;
    this.messageId = messageId;
    this.roleId = roleId;
    this.emojiId = emojiId;
  }
}