package com.example.zhiyiislandbackend.handler;

import com.example.zhiyiislandbackend.model.enums.NoteCategoryEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * NoteCategoryEnum 类型处理器
 */
@MappedTypes(NoteCategoryEnum.class)
public class NoteCategoryTypeHandler extends BaseTypeHandler<NoteCategoryEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NoteCategoryEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public NoteCategoryEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return NoteCategoryEnum.fromCode(code);
    }

    @Override
    public NoteCategoryEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return NoteCategoryEnum.fromCode(code);
    }

    @Override
    public NoteCategoryEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return NoteCategoryEnum.fromCode(code);
    }
}
