package com.example.zhiyiislandbackend.handler;

import com.example.zhiyiislandbackend.model.enums.NoteStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * NoteStatusEnum 类型处理器
 */
@MappedTypes(NoteStatusEnum.class)
public class NoteStatusTypeHandler extends BaseTypeHandler<NoteStatusEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NoteStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public NoteStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return NoteStatusEnum.fromCode(code);
    }

    @Override
    public NoteStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return NoteStatusEnum.fromCode(code);
    }

    @Override
    public NoteStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return NoteStatusEnum.fromCode(code);
    }
}
