package com.example.zhiyiislandbackend.handler;

import com.example.zhiyiislandbackend.model.enums.ImportStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ImportStatusEnum 类型处理器
 */
@MappedTypes(ImportStatusEnum.class)
public class ImportStatusTypeHandler extends BaseTypeHandler<ImportStatusEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ImportStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public ImportStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return ImportStatusEnum.fromCode(code);
    }

    @Override
    public ImportStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return ImportStatusEnum.fromCode(code);
    }

    @Override
    public ImportStatusEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return ImportStatusEnum.fromCode(code);
    }
}
