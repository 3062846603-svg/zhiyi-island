package com.example.zhiyiislandbackend.controller;

import com.example.zhiyiislandbackend.common.result.Result;
import com.example.zhiyiislandbackend.model.dto.user.request.*;
import com.example.zhiyiislandbackend.model.dto.user.response.LoginResponse;
import com.example.zhiyiislandbackend.model.dto.user.response.UserInfoResponse;
import com.example.zhiyiislandbackend.service.CodeService;
import com.example.zhiyiislandbackend.service.FileService;
import com.example.zhiyiislandbackend.service.TokenService;
import com.example.zhiyiislandbackend.service.UserService;
import com.example.zhiyiislandbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 * 处理用户认证、信息管理和账户相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final CodeService codeService;

    /**
     * 用户注册
     *
     * @param request 注册请求体
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("收到注册请求，用户名：{}", request.getUsername());
        userService.register(request);
        log.info("注册请求处理完成，用户名：{}", request.getUsername());
        return Result.success("注册成功", null);
    }

    /**
     * 检查用户名是否已存在
     * 用于注册时实时验证用户名可用性
     *
     * @param username 用户名
     * @return 用户名是否已存在
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        log.info("收到检查用户名请求，用户名：{}", username);
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(exists);
    }

    /**
     * 用户登录
     *
     * @param request 登录请求体
     * @return 登录响应（包含Token和用户信息）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("收到登录请求，用户名：{}", request.getUsername());
        LoginResponse response = userService.login(request);

        tokenService.saveToken(response.getToken(), response.getUserId());
        log.info("登录成功，Token已存入Redis，用户ID：{}", response.getUserId());

        return Result.success("登录成功", response);
    }

    /**
     * 验证码登录
     *
     * @param request 验证码登录请求体
     * @return 登录响应（包含Token和用户信息）
     */
    @PostMapping("/login/code")
    public Result<LoginResponse> loginWithCode(@Valid @RequestBody LoginWithCodeRequest request) {
        log.info("收到验证码登录请求，邮箱：{}", request.getEmail());
        LoginResponse response = userService.loginWithCode(request);

        tokenService.saveToken(response.getToken(), response.getUserId());
        log.info("验证码登录成功，Token已存入Redis，用户ID：{}", response.getUserId());

        return Result.success("登录成功", response);
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractToken(request);

        if (token != null) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            tokenService.invalidateAllTokensByUserId(userId);
            log.info("用户已登出，所有设备Token已失效，用户ID：{}", userId);
        }

        return Result.success("登出成功", null);
    }

    /**
     * 获取当前用户信息
     *
     * @param request HTTP请求
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo(HttpServletRequest request) {
        String token = extractToken(request);

        if (token == null || !tokenService.isTokenValid(token)) {
            log.warn("获取用户信息失败，未登录或Token无效");
            return Result.error(401, "请先登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("获取用户信息，用户ID：{}", userId);

        UserInfoResponse userInfo = userService.getUserInfoById(userId);
        return Result.success(userInfo);
    }

    /**
     * 修改密码
     *
     * @param request               HTTP请求
     * @param changePasswordRequest 修改密码请求体
     * @return 修改结果
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(HttpServletRequest request,
                                       @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        String token = extractToken(request);

        if (token == null || !tokenService.isTokenValid(token)) {
            log.warn("修改密码失败，未登录或Token无效");
            return Result.error(401, "请先登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("收到修改密码请求，用户ID：{}", userId);

        userService.updatePassword(userId, changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());

        tokenService.invalidateAllTokensByUserId(userId);
        log.info("密码修改成功，用户所有Token已失效，用户ID：{}", userId);

        return Result.success("密码修改成功，请重新登录", null);
    }

    /**
     * 通过验证码修改密码
     * 用于忘记密码场景，需要邮箱验证码验证身份
     *
     * @param request                    HTTP请求
     * @param changePasswordByCodeRequest 验证码修改密码请求体
     * @return 修改结果
     */
    @PutMapping("/password/by-code")
    public Result<Void> updatePasswordByCode(HttpServletRequest request,
                                             @Valid @RequestBody ChangePasswordByCodeRequest changePasswordByCodeRequest) {
        String token = extractToken(request);

        if (token == null || !tokenService.isTokenValid(token)) {
            log.warn("通过验证码修改密码失败，未登录或Token无效");
            return Result.error(401, "请先登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("收到通过验证码修改密码请求，用户ID：{}", userId);

        userService.updatePasswordByCode(userId, changePasswordByCodeRequest.getEmail(),
                changePasswordByCodeRequest.getCode(), changePasswordByCodeRequest.getNewPassword());

        tokenService.invalidateAllTokensByUserId(userId);
        log.info("通过验证码修改密码成功，用户所有Token已失效，用户ID：{}", userId);

        return Result.success("密码修改成功，请重新登录", null);
    }

    /**
     * 验证密码
     * 用于敏感操作前的身份验证
     *
     * @param request               HTTP请求
     * @param verifyPasswordRequest 验证密码请求体
     * @return 验证结果
     */
    @PostMapping("/password/verify")
    public Result<Void> verifyPassword(HttpServletRequest request,
                                       @Valid @RequestBody VerifyPasswordRequest verifyPasswordRequest) {
        String token = extractToken(request);

        if (token == null || !tokenService.isTokenValid(token)) {
            log.warn("验证密码失败，未登录或Token无效");
            return Result.error(401, "请先登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("收到验证密码请求，用户ID：{}", userId);

        userService.verifyPassword(userId, verifyPasswordRequest.getPassword());
        log.info("密码验证成功，用户ID：{}", userId);

        return Result.success("密码验证成功", null);
    }

    /**
     * 验证邮箱验证码
     * 用于换绑邮箱等需要验证邮箱的场景
     *
     * @param verifyEmailCodeRequest 验证邮箱验证码请求体
     * @return 验证结果
     */
    @PostMapping("/email/verify")
    public Result<Void> verifyEmailCode(@Valid @RequestBody VerifyEmailCodeRequest verifyEmailCodeRequest) {
        log.info("收到验证邮箱验证码请求，邮箱：{}", verifyEmailCodeRequest.getEmail());

        codeService.verifyCode(verifyEmailCodeRequest.getEmail(), verifyEmailCodeRequest.getCode());
        log.info("邮箱验证码验证成功，邮箱：{}", verifyEmailCodeRequest.getEmail());

        return Result.success("验证成功", null);
    }

    /**
     * 更新用户信息
     *
     * @param request              HTTP请求
     * @param updateProfileRequest 更新信息请求体
     * @return 更新结果
     */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(HttpServletRequest request,
                                       @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        String token = extractToken(request);

        if (token == null || !tokenService.isTokenValid(token)) {
            log.warn("更新用户信息失败，未登录或Token无效");
            return Result.error(401, "请先登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("收到更新用户信息请求，用户ID：{}，昵称：{}", userId, updateProfileRequest.getNickname());

        userService.updateUserInfo(userId, updateProfileRequest);
        log.info("更新用户信息请求处理完成，用户ID：{}", userId);
        return Result.success("信息更新成功", null);
    }

    /**
     * 更新邮箱（换绑邮箱）
     *
     * @param request            HTTP请求
     * @param updateEmailRequest 更新邮箱请求体
     * @return 更新结果
     */
    @PutMapping("/email")
    public Result<Void> updateEmail(HttpServletRequest request, @Valid @RequestBody UpdateEmailRequest updateEmailRequest) {
        String token = extractToken(request);

        if (token == null || !tokenService.isTokenValid(token)) {
            log.warn("更新邮箱失败，未登录或Token无效");
            return Result.error(401, "请先登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("收到更新邮箱请求，用户ID：{}，新邮箱：{}", userId, updateEmailRequest.getEmail());

        userService.updateEmail(userId, updateEmailRequest.getEmail(), updateEmailRequest.getCode());
        log.info("更新邮箱请求处理完成，用户ID：{}", userId);
        return Result.success("邮箱绑定成功", null);
    }

    /**
     * 发送邮箱验证码
     *
     * @param request 发送验证码请求体
     * @return 发送结果
     */
    @PostMapping("/email/code")
    public Result<Void> sendEmailCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送验证码请求，邮箱：{}", request.getEmail());

        codeService.sendCode(request.getEmail());
        log.info("验证码发送成功，邮箱：{}", request.getEmail());
        return Result.success("验证码已发送", null);
    }

    /**
     * 重置密码（忘记密码）
     *
     * @param request 重置密码请求体
     * @return 重置结果
     */
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("收到重置密码请求，邮箱：{}", request.getEmail());

        userService.resetPassword(request.getEmail(), request.getCode(), request.getNewPassword());
        log.info("重置密码成功，邮箱：{}", request.getEmail());
        return Result.success("密码重置成功", null);
    }

    /**
     * 上传头像
     *
     * @param request HTTP请求
     * @param file    头像文件
     * @return 头像URL
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(HttpServletRequest request,
                                       @RequestParam("file") MultipartFile file) {
        String token = extractToken(request);

        if (token == null || !tokenService.isTokenValid(token)) {
            log.warn("上传头像失败，未登录或Token无效");
            return Result.error(401, "请先登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("收到上传头像请求，用户ID：{}", userId);

        String avatarUrl = fileService.uploadAvatar(userId, file);

        UpdateProfileRequest profileRequest = new UpdateProfileRequest();
        profileRequest.setAvatar(avatarUrl);
        userService.updateUserInfo(userId, profileRequest);

        log.info("头像上传并更新成功，用户ID：{}", userId);
        return Result.success("头像上传成功", avatarUrl);
    }

    /**
     * 删除账户
     *
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/account")
    public Result<Void> deleteAccount(HttpServletRequest request) {
        String token = extractToken(request);

        if (token == null || !tokenService.isTokenValid(token)) {
            log.warn("删除账户失败，未登录或Token无效");
            return Result.error(401, "请先登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("收到删除账户请求，用户ID：{}", userId);

        tokenService.invalidateAllTokensByUserId(userId);
        userService.deleteAccount(userId);

        log.info("账户删除成功，用户ID：{}", userId);
        return Result.success("账户已删除", null);
    }

    /**
     * 从HTTP请求中提取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}
