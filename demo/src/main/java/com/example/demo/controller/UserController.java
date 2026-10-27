package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@RestController
@RequestMapping("/api/user")
public class UserController {

    @Value("${upload.path}") //
    private String uploadPath;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

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
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody User user) {
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
//获取用户
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
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
        return userRepository.findByRole(role);  // 根据角色返回用户
    }
        // 更新用户信息
    @PutMapping("/{id}/updateUser")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Optional<User> optionalUser = userRepository.findByUserId(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setAvatar(userDetails.getAvatar());
            user.setPhone(userDetails.getPhone());
            user.setAddress(userDetails.getAddress());
            user.setWalletBalance(userDetails.getWalletBalance());
            user.setRealName(userDetails.getRealName());
            user.setIdCardNumber(userDetails.getIdCardNumber());
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        } else {
            // 添加新用户，设置默认密码
            userDetails.setPassword(passwordEncoder.encode("123456"));  // 设置默认密码
            User newUser = userRepository.save(userDetails);
            return ResponseEntity.ok(newUser);
        }
    }
    //更新头像
    @PostMapping("/{userId}/uploadAvatar")
    public ResponseEntity<String> uploadAvatar(@PathVariable Integer userId, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("文件不能为空");
        }
        Optional<User> userOpt = userRepository.findById(userId);
        String currentAvatarUrl = userOpt.map(User::getAvatar).orElse(null);

        String currentAvatarPath = currentAvatarUrl != null ? currentAvatarUrl.replace("http://localhost:10202/images/avatars/", "src/main/resources/static/images/avatars/") : null;
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFileName = UUID.randomUUID().toString() + fileExtension; // 生成随机文件名

        // 定义文件存储路径
        String directory = "src/main/resources/static/images/avatars/";
        Path filePath = Paths.get(directory + newFileName);

        try {
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            String avatarUrl = "http://localhost:10202/images/avatars/" + newFileName;
            userService.updateUserAvatar(userId, avatarUrl);
            if (currentAvatarPath != null) {
                Path currentFilePath = Paths.get(currentAvatarPath);
                Files.deleteIfExists(currentFilePath); // 删除原头像文件
            }
            return ResponseEntity.ok(avatarUrl);
        } catch (MaxUploadSizeExceededException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("文件过大，请选择小于 5MB 的文件");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("文件保存失败: " + e.getMessage());
        }
    }

    //获取实名信息
    @GetMapping("/{id}/getrealname")
    public ResponseEntity<Map<String, String>> getRealNameInfo(@PathVariable Integer id) {
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
        // 获取充值金额，使用 double 类型
        double amount = Double.parseDouble(request.get("amount").toString());
        // 检查充值金额是否合法
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("充值金额无效");
        }
        // 调用服务层的充值逻辑
        boolean success = userService.rechargeUserAccount(userId, amount);
        if (success) {
            return ResponseEntity.ok().body("充值成功");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("充值失败");
        }
    }
    //更改密码
    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> request) {
        Integer userId = (Integer) request.get("id");
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
        // 获取要更新的金额（正数代表充值，负数代表扣款）
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        // 调用服务层的更新余额逻辑
        boolean success = userService.updateUserBalance(id, amount);
        if (success) {
            return ResponseEntity.ok("余额更新成功");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("余额更新失败");
        }
    }
    //删除用户
    @DeleteMapping("/delete")
    public ResponseEntity<?> batchDelete(@RequestBody List<Integer> userIds) {
        try {
            userService.batchDeleteUsers(userIds);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除失败");
        }
    }
}


