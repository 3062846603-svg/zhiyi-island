package com.example.zhiyiislandbackend.mapper;

import com.example.zhiyiislandbackend.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问接口
 * 提供用户表的CRUD操作
 */
@Mapper
public interface UserMapper {

    /**
     * 插入新用户
     */
    int insert(User user);

    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 更新用户信息
     */
    int updateById(User user);

    /**
     * 更新用户密码
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新用户头像
     */
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") Long id);
}
