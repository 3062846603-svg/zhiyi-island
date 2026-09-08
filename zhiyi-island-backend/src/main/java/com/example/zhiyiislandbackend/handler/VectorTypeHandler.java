package com.example.zhiyiislandbackend.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 向量类型处理器，用于在 Java 的 {@code float[]} 与 PostgreSQL 的 {@code vector} 类型之间进行转换。
 * <p>
 * 该类继承自 MyBatis 的 {@link BaseTypeHandler}，并声明如下映射关系：
 * <ul>
 *     <li>{@link MappedTypes}：将 Java 类型 {@code float[]} 映射到该处理器；</li>
 *     <li>{@link MappedJdbcTypes}：将 JDBC 类型 {@link JdbcType#OTHER}（PostgreSQL 的扩展类型）映射到该处理器。</li>
 * </ul>
 * 在写入数据库时，将 {@code float[]} 转换为 PostgreSQL 向量文本格式 {@code [x1,x2,...,xn]}；
 * 在读取数据库时，将该文本格式解析回 {@code float[]}，从而支撑向量相似度检索等场景。
 */
@MappedTypes(float[].class)
@MappedJdbcTypes(JdbcType.OTHER)
public class VectorTypeHandler extends BaseTypeHandler<float[]> {

    /**
     * 将非空的 Java {@code float[]} 参数写入 PreparedStatement，供 MyBatis 在插入或更新时调用。
     * <p>
     * 该方法把数组拼接为 PostgreSQL 向量文本格式 {@code [x1,x2,...,xn]}，并以 {@link java.sql.Types#OTHER}
     * 类型通过 {@link PreparedStatement#setObject(int, Object, int)} 写入，从而适配 PostgreSQL 的 {@code vector} 列。
     *
     * @param ps        预编译的 SQL 语句对象，用于设置参数
     * @param i         参数在 SQL 语句中的位置（从 1 开始）
     * @param parameter 待写入的非空 {@code float[]} 向量数据
     * @param jdbcType  对应的 JDBC 类型（此处为 {@link JdbcType#OTHER}）
     * @throws SQLException 当数据库访问发生异常时抛出
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        // 构造 PostgreSQL 向量文本格式的起始符号 "["
        StringBuilder sb = new StringBuilder("[");
        // 遍历向量数组，将每个浮点数依次追加
        for (int j = 0; j < parameter.length; j++) {
            // 从第二个元素起，在前面追加逗号分隔符，避免首个元素前置逗号
            if (j > 0) sb.append(",");
            // 追加当前维度的浮点数值
            sb.append(parameter[j]);
        }
        // 追加向量文本格式的结束符号 "]"
        sb.append("]");
        // 以 OTHER 类型（PostgreSQL 扩展类型）将拼接好的向量文本写入第 i 个参数位置
        ps.setObject(i, sb.toString(), java.sql.Types.OTHER);
    }

    /**
     * 根据列名从 ResultSet 中读取可能为空的向量结果，供 MyBatis 在查询映射时调用。
     *
     * @param rs         查询结果集
     * @param columnName 向量列的名称
     * @return 解析后的 {@code float[]}；若列值为空则返回 {@code null}
     * @throws SQLException 当数据库访问发生异常时抛出
     */
    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 以字符串形式读取 PostgreSQL 向量文本
        String value = rs.getString(columnName);
        // 将向量文本解析为 float[]
        return parseVector(value);
    }

    /**
     * 根据列索引从 ResultSet 中读取可能为空的向量结果，供 MyBatis 在查询映射时调用。
     *
     * @param rs          查询结果集
     * @param columnIndex 向量列的索引（从 1 开始）
     * @return 解析后的 {@code float[]}；若列值为空则返回 {@code null}
     * @throws SQLException 当数据库访问发生异常时抛出
     */
    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 以字符串形式读取 PostgreSQL 向量文本
        String value = rs.getString(columnIndex);
        // 将向量文本解析为 float[]
        return parseVector(value);
    }

    /**
     * 根据列索引从 CallableStatement 中读取可能为空的向量结果，供 MyBatis 调用存储过程时调用。
     *
     * @param cs          可调用语句对象
     * @param columnIndex 向量列的索引（从 1 开始）
     * @return 解析后的 {@code float[]}；若列值为空则返回 {@code null}
     * @throws SQLException 当数据库访问发生异常时抛出
     */
    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 以字符串形式读取 PostgreSQL 向量文本
        String value = cs.getString(columnIndex);
        // 将向量文本解析为 float[]
        return parseVector(value);
    }

    /**
     * 将 PostgreSQL 的向量文本（如 {@code [0.1,0.2,0.3]}）解析为 Java 的 {@code float[]}。
     * <p>
     * 该方法为内部工具方法，负责去除文本中的方括号、按逗号拆分并转换为浮点数数组；
     * 当输入为空时返回 {@code null}，以兼容数据库中向量列可能为空的情况。
     *
     * @param value PostgreSQL 向量文本字符串，可能为 {@code null} 或空串
     * @return 解析得到的 {@code float[]}；若输入为空则返回 {@code null}
     */
    private float[] parseVector(String value) {
        // 输入为空或空串时直接返回 null，避免空指针异常
        if (value == null || value.isEmpty()) {
            return null;
        }
        // 去除文本中的 "[" 和 "]" 字符，仅保留数值与分隔符
        value = value.replaceAll("[\\[\\]]", "");
        // 按逗号将向量文本拆分为独立的数值字符串
        String[] parts = value.split(",");
        // 创建与拆分数量一致的浮点数组用于存放结果
        float[] result = new float[parts.length];
        // 遍历拆分得到的字符串，逐个转换为浮点数
        for (int i = 0; i < parts.length; i++) {
            // 去除首尾空白后解析为 float，避免多余空格导致解析失败
            result[i] = Float.parseFloat(parts[i].trim());
        }
        // 返回解析完成的浮点数组
        return result;
    }
}
