package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.ImportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 导入记录数据访问接口
 * 提供导入记录表的CRUD操作
 */
@Mapper
public interface ImportRecordMapper {

    /**
     * 插入导入记录
     */
    int insert(ImportRecord record);

    /**
     * 根据ID查询导入记录
     */
    ImportRecord selectById(@Param("id") Long id);

    /**
     * 查询用户的导入历史
     */
    List<ImportRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 更新导入状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status,
                     @Param("noteCount") Integer noteCount, @Param("knowledgeCount") Integer knowledgeCount,
                     @Param("errorMessage") String errorMessage);

    /**
     * 删除导入记录
     */
    int deleteById(@Param("id") Long id);
}
