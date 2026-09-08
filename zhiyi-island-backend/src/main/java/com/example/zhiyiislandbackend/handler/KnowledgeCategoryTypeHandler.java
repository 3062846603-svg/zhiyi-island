package com.example.zhiyiislandbackend.handler;

import com.example.zhiyiislandbackend.model.enums.KnowledgeCategoryEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * KnowledgeCategoryEnum 类型处理器
 */
@MappedTypes(KnowledgeCategoryEnum.class)
public class KnowledgeCategoryTypeHandler extends BaseTypeHandler<KnowledgeCategoryEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, KnowledgeCategoryEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public KnowledgeCategoryEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return KnowledgeCategoryEnum.fromCode(code);
    }

    @Override
    public KnowledgeCategoryEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return KnowledgeCategoryEnum.fromCode(code);
    }

    @Override
    public KnowledgeCategoryEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return KnowledgeCategoryEnum.fromCode(code);
    }
}
