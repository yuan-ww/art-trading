package com.example.demo.service;
import com.example.demo.model.User;
import com.example.demo.model.WalletTransaction;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletTransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service public class WalletService {
 private final UserRepository users; private final WalletTransactionRepository transactions;
 public WalletService(UserRepository users, WalletTransactionRepository transactions){this.users=users;this.transactions=transactions;}
 @Transactional public User topUp(Integer userId, BigDecimal amount, String note, String requestKey){
  if(amount==null||amount.signum()<=0||amount.stripTrailingZeros().scale()>2) throw new IllegalArgumentException("充值金额必须大于 0，最多两位小数");
  if (requestKey != null && !requestKey.isBlank()) {
   if (requestKey.length() > 80) throw new IllegalArgumentException("请求标识过长");
   Optional<WalletTransaction> previous = transactions.findByRequestKey(requestKey);
   if (previous.isPresent()) {
    WalletTransaction existing = previous.get();
    if (!userId.equals(existing.getUserId()) || existing.getType() != WalletTransaction.Type.TOP_UP || amount.compareTo(existing.getAmount()) != 0) {
     throw new IllegalArgumentException("请求标识已用于不同的充值请求");
    }
    return users.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
   }
  }
  User user=users.findByUserIdForUpdate(userId).orElseThrow(()->new IllegalArgumentException("用户不存在"));
  if (requestKey != null && !requestKey.isBlank()) {
   Optional<WalletTransaction> previous = transactions.findByRequestKey(requestKey);
   if (previous.isPresent()) {
    WalletTransaction existing = previous.get();
    if (!userId.equals(existing.getUserId()) || existing.getType() != WalletTransaction.Type.TOP_UP || amount.compareTo(existing.getAmount()) != 0) {
     throw new IllegalArgumentException("请求标识已用于不同的充值请求");
    }
    return user;
   }
  }
  BigDecimal balance=(user.getWalletBalance()==null?BigDecimal.ZERO:user.getWalletBalance()).add(amount); user.setWalletBalance(balance); users.save(user);
  WalletTransaction tx=new WalletTransaction(); tx.setUserId(userId);tx.setAmount(amount);tx.setBalanceAfter(balance);tx.setType(WalletTransaction.Type.TOP_UP);tx.setNote(note);tx.setRequestKey(blankToNull(requestKey));transactions.save(tx); return user;
 }
 public User topUp(Integer userId, BigDecimal amount, String note){ return topUp(userId, amount, note, null); }
 public List<WalletTransaction> history(Integer userId){return transactions.findByUserIdOrderByTransactionIdDesc(userId);}
 public Page<WalletTransaction> historyPage(Integer userId, Pageable pageable){ return transactions.findByUserId(userId, pageable); }
 public void record(User user, BigDecimal amount, WalletTransaction.Type type, String note){
  WalletTransaction tx=new WalletTransaction(); tx.setUserId(user.getUserId()); tx.setAmount(amount); tx.setBalanceAfter(user.getWalletBalance()); tx.setType(type); tx.setNote(note); transactions.save(tx);
 }
 private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
