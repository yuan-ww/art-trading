package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.example.demo.service.AccessControlService;
import com.example.demo.service.ImageUploadService;
import com.example.demo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Value("${upload.path}") //
    private String uploadPath;
    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccessControlService accessControlService;
    @Autowired
    private ImageUploadService imageUploadService;
    @Autowired
    private WalletService walletService;

    @GetMapping("/checkUsername")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String username, @RequestParam Integer userId) {
        boolean exists = userRepository.existsByUsernameAndUserIdNot(username, userId);
        return ResponseEntity.ok(exists);
    }
    @GetMapping("/checkEmail")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email, @RequestParam Integer userId) {
        boolean exists = userRepository.existsByEmailAndUserIdNot(email, userId);
        return ResponseEntity.ok(exists);
    }
    @PostMapping("/add")
    public int registerUser(@RequestBody User user) {
        return userService.registerUser(user); // 调用 UserService 处理注册
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody User user, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        // 1. 检查邮箱是否已注册
        if (!userRepository.existsByEmail(user.getEmail())) {
            response.put("status", -1); // 邮箱未注册
            return ResponseEntity.ok(response);
        }

        // 2. 检查密码是否正确
        boolean isAuthenticated = userService.authenticateUser(user.getEmail(), user.getPassword());
        if (isAuthenticated) {
            // 登录成功，获取用户信息
            User loggedInUser = userRepository.findByEmail(user.getEmail());
            if (loggedInUser.isDisabled()) {
                response.put("status", 2);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new UsernamePasswordAuthenticationToken(
                    loggedInUser.getUserId(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + loggedInUser.getRole()))
            ));
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            // 设置响应体
            response.put("status", 0); // 登录成功
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", loggedInUser.getUsername());
            userInfo.put("id", loggedInUser.getUserId());
            userInfo.put("avatarUrl", loggedInUser.getAvatar() != null ? loggedInUser.getAvatar() : ""); // 确保头像 URL 有效
            response.put("user", userInfo);

            return ResponseEntity.ok(response);
        } else {
            response.put("status", 1); // 密码错误
            return ResponseEntity.ok(response);
        }

    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
//获取用户
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        accessControlService.requireSelfOrAdmin(id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
//
    @GetMapping("/getUsersByRole")
    public List<User> getUsersByRole(@RequestParam String role) {
        accessControlService.requireAdmin();
        return userRepository.findByRole(role);  // 根据角色返回用户
    }
    @GetMapping("/page")
    public Page<User> getUsersPage(@RequestParam(defaultValue = "User") String role,
                                   @RequestParam(defaultValue = "") String keyword,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        accessControlService.requireAdmin();
        return userRepository.findByRoleAndUsernameContainingIgnoreCase(role, keyword.trim(),
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("userId").descending()));
    }
        // 更新用户信息
    @PutMapping("/{id}/updateUser")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        accessControlService.requireSelfOrAdmin(id);
        Optional<User> optionalUser = userRepository.findByUserId(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            user.setAddress(userDetails.getAddress());
            user.setRealName(userDetails.getRealName());
            user.setIdCardNumber(userDetails.getIdCardNumber());
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    //更新头像
    @PostMapping("/{userId}/uploadAvatar")
    public ResponseEntity<String> uploadAvatar(@PathVariable Integer userId, @RequestParam("file") MultipartFile file) {
        accessControlService.requireSelfOrAdmin(userId);
        Optional<User> userOpt = userRepository.findById(userId);
        String currentAvatarUrl = userOpt.map(User::getAvatar).orElse(null);
        Path avatarDirectory = Paths.get(uploadPath, "avatars").toAbsolutePath().normalize();
        String avatarUrl = imageUploadService.store(file, avatarDirectory, publicBaseUrl + "/images/avatars/");
        try {
            userService.updateUserAvatar(userId, avatarUrl);
        } catch (RuntimeException e) {
            // 新文件已写入但数据库未更新时，避免留下不可访问的头像文件。
            deletePreviousUploadedAvatar(avatarUrl, avatarDirectory);
            throw e;
        }
        deletePreviousUploadedAvatar(currentAvatarUrl, avatarDirectory);
        return ResponseEntity.ok(avatarUrl);
    }
    @PutMapping("/{id}/disabled")
    public ResponseEntity<String> setDisabled(@PathVariable Integer id, @RequestParam boolean disabled) {
        accessControlService.requireAdmin();
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if ("Admin".equals(user.getRole())) return ResponseEntity.badRequest().body("不能禁用管理员");
        user.setDisabled(disabled);
        userRepository.save(user);
        return ResponseEntity.ok(disabled ? "用户已禁用" : "用户已启用");
    }

    private void deletePreviousUploadedAvatar(String avatarUrl, Path avatarDirectory) {
        String prefix = publicBaseUrl + "/images/avatars/";
        if (avatarUrl == null || !avatarUrl.startsWith(prefix)) {
            return;
        }
        String fileName = avatarUrl.substring(prefix.length());
        if (!fileName.matches("[0-9a-fA-F-]{36}\\.(jpg|jpeg|png|gif)")) {
            return;
        }
        Path previousAvatar = avatarDirectory.resolve(fileName).normalize();
        if (!previousAvatar.getParent().equals(avatarDirectory)) {
            return;
        }
        try {
            Files.deleteIfExists(previousAvatar);
        } catch (java.io.IOException ignored) {
            // 旧头像删除失败不影响新头像的保存。
        }
    }

    //获取实名信息
    @GetMapping("/{id}/getrealname")
    public ResponseEntity<Map<String, String>> getRealNameInfo(@PathVariable Integer id) {
        accessControlService.requireSelfOrAdmin(id);
        Map<String, String> realNameInfo = userService.getRealNameInfo(id);

        if (realNameInfo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(realNameInfo);
    }
    //充值
    @PostMapping("/recharge")
    public ResponseEntity<?> recharge(@RequestBody Map<String, Object> request) {
        // 获取用户 ID
        int userId = Integer.parseInt(request.get("id").toString());
        accessControlService.requireSelfOrAdmin(userId);
        // 获取充值金额，使用 double 类型
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        // 检查充值金额是否合法
        if (amount.signum() <= 0) {
            return ResponseEntity.badRequest().body("充值金额无效");
        }
        // 调用服务层的充值逻辑
        walletService.topUp(userId, amount, "模拟充值");
        return ResponseEntity.ok().body("充值成功");
    }
    //更改密码
    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> request) {
        Integer userId = (Integer) request.get("id");
        accessControlService.requireSelfOrAdmin(userId);
        String currentPassword = (String) request.get("currentPassword");
        String newPassword = (String) request.get("newPassword");
        boolean success = userService.updatePassword(userId, currentPassword, newPassword);
        if (success) {
            return ResponseEntity.ok("密码更新成功");
        } else {
            return ResponseEntity.ok("原密码不正确");
        }
    }
    @PostMapping("/{id}/updateBalance")
    public ResponseEntity<String> updateBalance(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        accessControlService.requireAdmin();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("请使用有流水记录的充值流程");
    }
    //删除用户
    @DeleteMapping("/delete")
    public ResponseEntity<?> batchDelete(@RequestBody List<Integer> userIds) {
        accessControlService.requireAdmin();
        try {
            userService.batchDeleteUsers(userIds);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除失败");
        }
    }
}


