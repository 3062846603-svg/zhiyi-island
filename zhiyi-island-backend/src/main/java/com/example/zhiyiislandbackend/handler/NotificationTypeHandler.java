package com.example.zhiyiislandbackend.handler;

import com.example.zhiyiislandbackend.model.enums.NotificationTypeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * NotificationTypeEnum 类型处理器
 */
@MappedTypes(NotificationTypeEnum.class)
public class NotificationTypeHandler extends BaseTypeHandler<NotificationTypeEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NotificationTypeEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public NotificationTypeEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return NotificationTypeEnum.fromCode(code);
    }

    @Override
    public NotificationTypeEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return NotificationTypeEnum.fromCode(code);
    }

    @Override
    public NotificationTypeEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return NotificationTypeEnum.fromCode(code);
    }
}
