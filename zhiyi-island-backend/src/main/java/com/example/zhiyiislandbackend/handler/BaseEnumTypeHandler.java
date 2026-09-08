package com.example.zhiyiislandbackend.handler;

import com.example.zhiyiislandbackend.model.enums.BaseEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 通用枚举类型处理器
 * 处理实现 BaseEnum 接口的枚举类型与数据库整数值之间的转换
 *
 * @param <E> 枚举类型
 */
public abstract class BaseEnumTypeHandler<E extends Enum<E> & BaseEnum> extends BaseTypeHandler<E> {

    /**
     * 枚举类
     */
    private final Class<E> enumClass;

    /**
     * 构造函数
     *
     * @param enumClass 枚举类
     */
    public BaseEnumTypeHandler(Class<E> enumClass) {
        if (enumClass == null) {
            throw new IllegalArgumentException("枚举类不能为空");
        }
        this.enumClass = enumClass;
    }

    /**
     * 设置非空参数
     * 将枚举转换为整数值存入数据库
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    /**
     * 根据列名获取可为空的结果
     * 从数据库读取整数值并转换为枚举
     */
    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Integer value = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return fromValue(value);
    }

    /**
     * 根据列索引获取可为空的结果
     * 从数据库读取整数值并转换为枚举
     */
    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Integer value = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return fromValue(value);
    }

    /**
     * 从存储过程获取可为空的结果
     * 从数据库读取整数值并转换为枚举
     */
    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Integer value = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        return fromValue(value);
    }

    /**
     * 根据值获取枚举
     *
     * @param value 数据库存储的值
     * @return 对应的枚举
     */
    private E fromValue(Integer value) {
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getCode().equals(value)) {
                return enumConstant;
            }
        }
        return null;
    }
}
