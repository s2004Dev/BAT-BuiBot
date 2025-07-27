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
import java.time.MonthDay;

@MappedTypes(MonthDay.class)
public class MonthDayTypeHandler extends BaseTypeHandler<MonthDay> {
  @Override
  public void setNonNullParameter(final @NotNull PreparedStatement ps, final int i, final @NotNull MonthDay parameter,
                                  final @Nullable JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter.toString());
  }

  @Override
  public @Nullable MonthDay getNullableResult(final @NotNull ResultSet rs, final @NotNull String columnName)
      throws SQLException {
    val dbValue = rs.getString(columnName);
    return dbValue == null ? null : MonthDay.parse(dbValue);
  }

  @Override
  public @Nullable MonthDay getNullableResult(final @NotNull ResultSet rs, final int columnIndex)
      throws SQLException {
    val dbValue = rs.getString(columnIndex);
    return dbValue == null ? null : MonthDay.parse(dbValue);
  }

  @Override
  public @Nullable MonthDay getNullableResult(final @NotNull CallableStatement cs, final int columnIndex)
      throws SQLException {
    val dbValue = cs.getString(columnIndex);
    return dbValue == null ? null : MonthDay.parse(dbValue);
  }
}