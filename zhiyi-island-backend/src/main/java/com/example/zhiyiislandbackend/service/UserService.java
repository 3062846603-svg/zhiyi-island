package com.example.zhiyiislandbackend.service;

import com.example.zhiyiislandbackend.model.dto.user.request.LoginRequest;
import com.example.zhiyiislandbackend.model.dto.user.request.LoginWithCodeRequest;
import com.example.zhiyiislandbackend.model.dto.user.request.RegisterRequest;
import com.example.zhiyiislandbackend.model.dto.user.request.UpdateProfileRequest;
import com.example.zhiyiislandbackend.model.dto.user.response.LoginResponse;
import com.example.zhiyiislandbackend.model.dto.user.response.UserInfoResponse;
import com.example.zhiyiislandbackend.model.entity.User;

/**
 * 用户服务接口
 * 定义用户相关的业务操作，包括认证、信息管理和账户操作
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param request 注册请求信息
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求信息
     * @return 登录响应（包含Token和用户信息）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 验证码登录
     *
     * @param request 验证码登录请求信息
     * @return 登录响应（包含Token和用户信息）
     */
    LoginResponse loginWithCode(LoginWithCodeRequest request);

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户实体
     */
    User getUserById(Long id);

    /**
     * 根据ID获取用户信息响应
     *
     * @param id 用户ID
     * @return 用户信息响应DTO
     */
    UserInfoResponse getUserInfoById(Long id);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getUserByUsername(String username);

    /**
     * 修改密码
     *
     * @param id          用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(Long id, String oldPassword, String newPassword);

    /**
     * 验证密码
     *
     * @param id       用户ID
     * @param password 密码
     */
    void verifyPassword(Long id, String password);

    /**
     * 通过验证码修改密码
     *
     * @param id          用户ID
     * @param email       邮箱
     * @param code        验证码
     * @param newPassword 新密码
     */
    void updatePasswordByCode(Long id, String email, String code, String newPassword);

    /**
     * 更新用户信息
     *
     * @param id      用户ID
     * @param request 更新请求信息
     */
    void updateUserInfo(Long id, UpdateProfileRequest request);

    /**
     * 更新邮箱（换绑邮箱）
     *
     * @param id    用户ID
     * @param email 新邮箱
     * @param code  验证码
     */
    void updateEmail(Long id, String email, String code);

    /**
     * 删除账户
     *
     * @param id 用户ID
     */
    void deleteAccount(Long id);

    /**
     * 重置密码（忘记密码）
     *
     * @param email       用户邮箱
     * @param code        验证码
     * @param newPassword 新密码
     */
    void resetPassword(String email, String code, String newPassword);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean checkUsernameExists(String username);
}
