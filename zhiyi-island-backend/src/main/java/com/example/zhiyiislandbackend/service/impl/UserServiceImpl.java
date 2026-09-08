package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.model.dto.user.request.LoginRequest;
import com.example.zhiyiislandbackend.model.dto.user.request.LoginWithCodeRequest;
import com.example.zhiyiislandbackend.model.dto.user.request.RegisterRequest;
import com.example.zhiyiislandbackend.model.dto.user.request.UpdateProfileRequest;
import com.example.zhiyiislandbackend.model.dto.user.response.LoginResponse;
import com.example.zhiyiislandbackend.model.dto.user.response.UserInfoResponse;
import com.example.zhiyiislandbackend.model.entity.User;
import com.example.zhiyiislandbackend.model.enums.UserStatusEnum;
import com.example.zhiyiislandbackend.exception.BusinessException;
import com.example.zhiyiislandbackend.mapper.UserMapper;
import com.example.zhiyiislandbackend.service.CodeService;
import com.example.zhiyiislandbackend.service.UserService;
import com.example.zhiyiislandbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑，包括注册、登录、信息管理和账户操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CodeService codeService;

    /**
     * 用户注册
     * 1. 验证邮箱验证码
     * 2. 检查用户名和邮箱是否已存在
     * 3. 创建用户并保存到数据库
     */
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = generateRandomUsername();
            log.info("用户未提供用户名，自动生成：{}", username);
        }
        log.info("用户注册开始，用户名：{}", username);

        if (!codeService.verifyCode(request.getEmail(), request.getCode())) {
            log.warn("注册失败，验证码错误或已过期：{}", request.getEmail());
            throw new BusinessException(400, "验证码错误或已过期");
        }

        User existUser = userMapper.selectByUsername(username);
        if (existUser != null) {
            log.warn("注册失败，用户名已存在：{}", username);
            throw new BusinessException(400, "用户名已存在");
        }

        User existEmail = userMapper.selectByEmail(request.getEmail());
        if (existEmail != null) {
            log.warn("注册失败，邮箱已被注册：{}", request.getEmail());
            throw new BusinessException(400, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : generateRandomChineseNickname());
        user.setStatus(UserStatusEnum.NORMAL);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功，用户ID：{}，用户名：{}，昵称：{}", user.getId(), user.getUsername(), user.getNickname());
    }

    /**
     * 生成随机用户名
     * 生成6-8位纯数字用户名
     *
     * @return 随机用户名
     */
    private String generateRandomUsername() {
        Random random = new Random();
        int length = 6 + random.nextInt(3);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成随机中文昵称
     * 从预设的形容词和名词中随机组合生成昵称
     *
     * @return 随机中文昵称
     */
    private String generateRandomChineseNickname() {
        String[] adjectives = {
            "快乐的", "聪明的", "勇敢的", "可爱的", "温柔的",
            "阳光的", "淡定的", "睿智的", "活泼的", "机智的",
            "幽默的", "淡然的", "沉稳的", "优雅的", "淡雅的",
            "清新的", "淡泊的", "宁静的", "恬淡的", "从容的"
        };
        String[] nouns = {
            "小熊", "小猫", "小狗", "小兔", "小鸟",
            "小鱼", "小虎", "小龙", "小凤", "小鹤",
            "小鹿", "小狐", "小豹", "小鹰", "小鲸",
            "小松鼠", "小企鹅", "小海豚", "小蝴蝶", "小蜜蜂"
        };
        Random random = new Random();
        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        int suffix = random.nextInt(100);
        return adjective + noun + suffix;
    }

    /**
     * 用户登录
     * 支持用户名或邮箱登录
     * 1. 根据输入判断是用户名还是邮箱
     * 2. 验证密码
     * 3. 检查账号状态
     * 4. 生成JWT Token
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录开始，用户名：{}", request.getUsername());

        User user = null;
        String input = request.getUsername();

        if (input.contains("@")) {
            user = userMapper.selectByEmail(input);
        } else {
            user = userMapper.selectByUsername(input);
        }

        if (user == null) {
            log.warn("登录失败，用户不存在：{}", request.getUsername());
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败，密码错误，用户名：{}", request.getUsername());
            throw new BusinessException(400, "用户名或密码错误");
        }

        if (user.getStatus() != UserStatusEnum.NORMAL) {
            log.warn("登录失败，账号已被禁用，用户名：{}", request.getUsername());
            throw new BusinessException(400, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("用户登录成功，用户ID：{}，用户名：{}", user.getId(), user.getUsername());

        return LoginResponse.builder()
                .userId(user.getId())
                .token(token)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .build();
    }

    /**
     * 验证码登录
     * 1. 验证邮箱验证码
     * 2. 查找用户
     * 3. 检查账号状态
     * 4. 生成JWT Token
     */
    @Override
    public LoginResponse loginWithCode(LoginWithCodeRequest request) {
        log.info("验证码登录开始，邮箱：{}", request.getEmail());

        if (!codeService.verifyCode(request.getEmail(), request.getCode())) {
            log.warn("验证码登录失败，验证码错误或已过期：{}", request.getEmail());
            throw new BusinessException(400, "验证码错误或已过期");
        }

        User user = userMapper.selectByEmail(request.getEmail());
        if (user == null) {
            log.warn("验证码登录失败，邮箱未注册：{}", request.getEmail());
            throw new BusinessException(400, "该邮箱未注册");
        }

        if (user.getStatus() != UserStatusEnum.NORMAL) {
            log.warn("验证码登录失败，账号已被禁用，邮箱：{}", request.getEmail());
            throw new BusinessException(400, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("验证码登录成功，用户ID：{}，邮箱：{}", user.getId(), request.getEmail());

        return LoginResponse.builder()
                .userId(user.getId())
                .token(token)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .build();
    }

    /**
     * 根据ID获取用户实体
     */
    @Override
    public User getUserById(Long id) {
        log.debug("查询用户信息，用户ID：{}", id);
        return userMapper.selectById(id);
    }

    /**
     * 根据ID获取用户信息响应DTO
     */
    @Override
    public UserInfoResponse getUserInfoById(Long id) {
        log.debug("查询用户信息，用户ID：{}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createTime(user.getCreateTime() != null ? user.getCreateTime().toString() : null)
                .build();
    }

    /**
     * 根据用户名获取用户实体
     */
    @Override
    public User getUserByUsername(String username) {
        log.debug("查询用户信息，用户名：{}", username);
        return userMapper.selectByUsername(username);
    }

    /**
     * 修改密码
     * 验证旧密码后更新新密码
     */
    @Override
    @Transactional
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        log.info("修改密码开始，用户ID：{}", id);

        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("修改密码失败，用户不存在，用户ID：{}", id);
            throw new BusinessException(400, "用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("修改密码失败，原密码错误，用户ID：{}", id);
            throw new BusinessException(400, "原密码错误");
        }

        userMapper.updatePassword(id, passwordEncoder.encode(newPassword));
        log.info("修改密码成功，用户ID：{}", id);
    }

    /**
     * 验证密码
     * 验证用户输入的密码是否正确
     */
    @Override
    public void verifyPassword(Long id, String password) {
        log.info("验证密码开始，用户ID：{}", id);

        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("验证密码失败，用户不存在，用户ID：{}", id);
            throw new BusinessException(400, "用户不存在");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("验证密码失败，密码错误，用户ID：{}", id);
            throw new BusinessException(400, "密码错误");
        }

        log.info("验证密码成功，用户ID：{}", id);
    }

    /**
     * 通过验证码修改密码
     * 需要验证邮箱验证码
     */
    @Override
    @Transactional
    public void updatePasswordByCode(Long id, String email, String code, String newPassword) {
        log.info("通过验证码修改密码开始，用户ID：{}，邮箱：{}", id, email);

        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("通过验证码修改密码失败，用户不存在，用户ID：{}", id);
            throw new BusinessException(400, "用户不存在");
        }

        if (!email.equals(user.getEmail())) {
            log.warn("通过验证码修改密码失败，邮箱不匹配，用户ID：{}，邮箱：{}", id, email);
            throw new BusinessException(400, "邮箱与当前账户不匹配");
        }

        if (!codeService.verifyCode(email, code)) {
            log.warn("通过验证码修改密码失败，验证码错误或已过期：{}", email);
            throw new BusinessException(400, "验证码错误或已过期");
        }

        userMapper.updatePassword(id, passwordEncoder.encode(newPassword));
        log.info("通过验证码修改密码成功，用户ID：{}", id);
    }

    /**
     * 更新用户信息
     * 支持更新昵称、头像、手机号、简介、位置、网站等字段
     */
    @Override
    @Transactional
    public void updateUserInfo(Long id, UpdateProfileRequest request) {
        log.info("更新用户信息开始，用户ID：{}", id);

        User user = new User();
        user.setId(id);
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setPhone(request.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("更新用户信息成功，用户ID：{}", id);
    }

    /**
     * 更新邮箱（换绑邮箱）
     * 需要验证新邮箱的验证码
     */
    @Override
    @Transactional
    public void updateEmail(Long id, String email, String code) {
        log.info("更新邮箱开始，用户ID：{}，新邮箱：{}", id, email);

        if (!codeService.verifyCode(email, code)) {
            log.warn("更新邮箱失败，验证码错误或已过期：{}", email);
            throw new BusinessException(400, "验证码错误或已过期");
        }

        User existEmail = userMapper.selectByEmail(email);
        if (existEmail != null && !existEmail.getId().equals(id)) {
            log.warn("更新邮箱失败，邮箱已被其他用户绑定：{}", email);
            throw new BusinessException(400, "邮箱已被其他用户绑定");
        }

        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("更新邮箱成功，用户ID：{}", id);
    }

    /**
     * 删除账户
     * 永久删除用户账户及所有相关数据
     */
    @Override
    @Transactional
    public void deleteAccount(Long id) {
        log.info("删除账户开始，用户ID：{}", id);

        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("删除账户失败，用户不存在，用户ID：{}", id);
            throw new BusinessException(400, "用户不存在");
        }

        userMapper.deleteById(id);
        log.info("删除账户成功，用户ID：{}", id);
    }

    /**
     * 重置密码（忘记密码）
     * 通过邮箱验证码重置密码
     */
    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        log.info("重置密码开始，邮箱：{}", email);

        if (!codeService.verifyCode(email, code)) {
            log.warn("重置密码失败，验证码错误或已过期：{}", email);
            throw new BusinessException(400, "验证码错误或已过期");
        }

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            log.warn("重置密码失败，邮箱未注册：{}", email);
            throw new BusinessException(400, "该邮箱未注册");
        }

        userMapper.updatePassword(user.getId(), passwordEncoder.encode(newPassword));
        log.info("重置密码成功，用户ID：{}，邮箱：{}", user.getId(), email);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        log.debug("检查用户名是否存在：{}", username);
        User user = userMapper.selectByUsername(username);
        return user != null;
    }
}
