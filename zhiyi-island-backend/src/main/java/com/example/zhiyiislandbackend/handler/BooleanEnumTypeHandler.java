package com.example.zhiyiislandbackend.handler;

import com.example.zhiyiislandbackend.model.enums.BooleanEnum;
import org.apache.ibatis.type.MappedTypes;

/**
 * BooleanEnum 类型处理器
 * 处理 BooleanEnum 枚举与数据库整数值之间的转换
 */
@MappedTypes(BooleanEnum.class)
public class BooleanEnumTypeHandler extends BaseEnumTypeHandler<BooleanEnum> {

    /**
     * 构造函数
     */
    public BooleanEnumTypeHandler() {
        super(BooleanEnum.class);
    }
}
