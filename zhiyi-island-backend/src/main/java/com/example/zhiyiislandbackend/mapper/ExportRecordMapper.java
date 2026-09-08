package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.ExportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 导出记录数据访问接口
 * 提供导出记录表的CRUD操作
 */
@Mapper
public interface ExportRecordMapper {

    /**
     * 插入导出记录
     */
    int insert(ExportRecord record);

    /**
     * 查询用户的导出历史
     */
    List<ExportRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 删除导出记录
     */
    int deleteById(@Param("id") Long id);
}
