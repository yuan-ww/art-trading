package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class UserService {
    @Value("${upload.path}")
    private String imagesPath;
    @Value("${app.public-base-url}")
    private String publicBaseUrl;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    // 注册用户
    public int registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail()) && userRepository.existsByUsername(user.getUsername())) {
            return -3;
        }else if(userRepository.existsByUsername(user.getUsername())){
            return -2;//用户名被注册
        }else if (userRepository.existsByEmail(user.getEmail())){
            return -1;// 邮箱被注册
        }else{
            user.setWalletBalance(BigDecimal.ZERO);
            user.setRole("User");
            String password = user.getPassword();
            if (password == null || password.trim().isEmpty()) {
                return -4;
            }
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return 0;
        }
    }
   //登录
    public void updateUserAvatar(Integer userId, String avatarUrl) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("用户未找到"));

        user.setAvatar(avatarUrl);
        userRepository.save(user);
    }
    public boolean authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }
    //获取实名信息
    public Map<String, String> getRealNameInfo(Integer id) {
        User user = userRepository.getUserByUserId(id);
        Map<String, String> realNameInfo = new HashMap<>();

        if (user != null) {
            realNameInfo.put("realName", user.getRealName());
            realNameInfo.put("idCardNumber", user.getIdCardNumber());
        }
        return realNameInfo;
    }
    //充值
    @Transactional
    public boolean rechargeUserAccount(Integer userId, double amount) {
        // 获取用户对象
        Optional<User> user = userRepository.findByUserIdForUpdate(userId);
        if (user.isEmpty()) {
            return false;
        }
        User currentUser = user.get();
        BigDecimal currentBalance = currentUser.getWalletBalance();
        // 将充值金额转换为 BigDecimal
        BigDecimal rechargeAmount = new BigDecimal(Double.toString(amount));
        // 更新用户余额
        BigDecimal newBalance = currentBalance.add(rechargeAmount);
        currentUser.setWalletBalance(newBalance);
        // 保存更新后的用户信息
        userRepository.save(currentUser);
        return true;
    }
    //改密码
    public boolean updatePassword(Integer userId, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false; // 用户不存在
        }
        User user = userOpt.get();
        // 验证原密码是否正确
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false; // 原密码不正确
        }
        // 更新新密码并加密
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }
    //删除用户
    @Transactional
    public void batchDeleteUsers(List<Integer> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            for (Integer userId : userIds) {
                try {
                    Optional<User> userOpt = userRepository.findByUserId(userId);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        String avatarUrl = user.getAvatar();
                        // 删除头像文件
                        if (avatarUrl != null && !avatarUrl.equals(publicBaseUrl + "/images/avatars/default.png")) {
                            deleteImageFile(avatarUrl);
                        };
                        // 删除用户记录
                        userRepository.delete(user);
                    } else {
                        throw new RuntimeException("用户记录未找到，无法删除! userId=" + userId);
                    }
                } catch (Exception e) {
                    throw e;  // 继续抛出异常
                }
            }
            userRepository.deleteAllByUserIdIn(userIds);
        } else {
            throw new IllegalArgumentException("用户ID列表为空");
        }
    }
    @Transactional
    public boolean updateUserBalance(Integer userId, BigDecimal amount) {
        Optional<User> optionalUser = userRepository.findByUserIdForUpdate(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            BigDecimal currentBalance = user.getWalletBalance();
            BigDecimal newBalance = currentBalance.add(amount);  // 更新余额

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {  // 确保余额不为负
                return false;
            }

            user.setWalletBalance(newBalance);  // 设置新余额
            userRepository.save(user);  // 保存更新后的用户信息
            return true;
        }
        return false;  // 用户不存在
    }
    private void deleteImageFile(String imageUrl) {
        try {
            // 处理文件 URL，去掉 "file:" 前缀（如果存在）
            if (imageUrl.startsWith("file:")) {
                imageUrl = imageUrl.substring(5);
            }
            // 提取文件名
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            // 创建文件路径
            Path filePath = Paths.get(imagesPath, "avatars", fileName);
            // 删除文件
            if (Files.exists(filePath)) {
                Files.delete(filePath);  // 删除文件
            } else {
                throw new RuntimeException("文件不存在: " + filePath.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("删除图片文件失败: " + imageUrl, e);
        }
    }
}



