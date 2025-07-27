package lonter.buibot.model.handlers;

import lombok.val;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;

@MappedTypes(ZoneId.class)
public class ZoneIdTypeHandler extends BaseTypeHandler<ZoneId> {
  @Override
  public void setNonNullParameter(final @NotNull PreparedStatement ps, final int i, final @NotNull ZoneId parameter,
                                  final @Nullable JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter.getId());
  }

  @Override
  public @Nullable ZoneId getNullableResult(final @NotNull ResultSet rs, final @NotNull String columnName)
      throws SQLException {
    val dbValue = rs.getString(columnName);
    return dbValue == null ? null : ZoneId.of(dbValue);
  }

  @Override
  public @Nullable ZoneId getNullableResult(final @NotNull ResultSet rs, final int columnIndex) throws SQLException {
    val dbValue = rs.getString(columnIndex);
    return dbValue == null ? null : ZoneId.of(dbValue);
  }

  @Override
  public @Nullable ZoneId getNullableResult(final @NotNull CallableStatement cs, final int columnIndex)
      throws SQLException {
    String dbValue = cs.getString(columnIndex);
    return dbValue == null ? null : ZoneId.of(dbValue);
  }
}