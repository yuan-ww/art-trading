package com.example.demo.controller;
import com.example.demo.model.User; import com.example.demo.model.WalletTransaction; import com.example.demo.service.AccessControlService; import com.example.demo.service.WalletService;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.util.List; import org.springframework.data.domain.Page; import org.springframework.data.domain.PageRequest; import org.springframework.data.domain.Sort;
@RestController @RequestMapping("/api/wallet") public class WalletController {
 private final WalletService wallet; private final AccessControlService access;
 public WalletController(WalletService wallet, AccessControlService access){this.wallet=wallet;this.access=access;}
 @PostMapping("/{userId}/top-up") public ResponseEntity<User> topUp(@PathVariable Integer userId,@RequestParam BigDecimal amount,@RequestParam(required=false) String note,@RequestHeader(value="Idempotency-Key",required=false) String requestKey){access.requireSelfOrAdmin(userId);return ResponseEntity.ok(wallet.topUp(userId,amount,note,requestKey));}
 @GetMapping("/{userId}/transactions") public List<WalletTransaction> history(@PathVariable Integer userId){access.requireSelfOrAdmin(userId);return wallet.history(userId);}
 @GetMapping("/{userId}/transactions/page") public Page<WalletTransaction> historyPage(@PathVariable Integer userId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){access.requireSelfOrAdmin(userId);return wallet.historyPage(userId,PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),Sort.by("transactionId").descending()));}
}
