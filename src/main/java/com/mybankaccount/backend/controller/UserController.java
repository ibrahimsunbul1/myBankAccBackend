package com.mybankaccount.backend.controller;

import com.mybankaccount.backend.dto.ChangePasswordRequest;
import com.mybankaccount.backend.dto.UpdateUserRequest;
import com.mybankaccount.backend.dto.UserRegistrationDto;
import com.mybankaccount.backend.controller.AuthController.LoginRequest;
import com.mybankaccount.backend.entity.User;
import com.mybankaccount.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "MyBankAccount API is running");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllActiveUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        
        if (user.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            User u = user.get();
            response.put("id", u.getId());
            response.put("username", u.getUsername());
            response.put("email", u.getEmail());
            response.put("firstName", u.getFirstName());
            response.put("lastName", u.getLastName());
            response.put("phoneNumber", u.getPhoneNumber());
            response.put("role", u.getRole());
            response.put("createdAt", u.getCreatedAt());
            response.put("isActive", u.getIsActive());
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UpdateUserRequest request, 
                                             Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        User userDetails = new User();
        userDetails.setUsername(request.getUsername());
        userDetails.setEmail(request.getEmail());
        userDetails.setFirstName(request.getFirstName());
        userDetails.setLastName(request.getLastName());
        userDetails.setPhoneNumber(request.getPhoneNumber());
        
        User updatedUser = userService.updateUser(currentUser.getId(), userDetails);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/change-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, 
                                          Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        userService.changePassword(currentUser.getId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully");
    }
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }
    
    @GetMapping("/check/{username}")
    public ResponseEntity<Map<String, Object>> checkUserExists(@PathVariable String username) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            response.put("exists", true);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("createdAt", user.getCreatedAt());
            return ResponseEntity.ok(response);
        } else {
            response.put("exists", false);
            response.put("message", "User not found");
            return ResponseEntity.ok(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginDto) {
        User user = null;
        String identifier = loginDto.getUsername();
        
        // Check if identifier is TC Kimlik No (11 digits)
        if (identifier.matches("\\d{11}")) {
            if (userService.isValidTcKimlikNo(identifier)) {
                Optional<User> userOptional = userService.findByTcKimlikNo(identifier);
                if (userOptional.isPresent()) {
                    user = userOptional.get();
                }
            } else if (userService.isValidCustomerNumber(identifier)) {
                Optional<User> userOptional = userService.findByMusteriNo(identifier);
                if (userOptional.isPresent()) {
                    user = userOptional.get();
                }
            }
        } else {
            // Try username login
            Optional<User> userOptional = userService.findByUsername(identifier);
            if (userOptional.isPresent()) {
                user = userOptional.get();
            }
        }
        
        if (user != null) {
            if (userService.validatePassword(loginDto.getPassword(), user.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "fullName", user.getFullName(),
                    "tcKimlikNo", user.getTcKimlikNo(),
                    "musteriNo", user.getMusteriNo()
                ));
                response.put("fullName", user.getFullName());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Şifre hatalı"));
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Kullanıcı bulunamadı", "needsRegistration", true));
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // Validate TC Kimlik No
        if (!userService.isValidTcKimlikNo(registrationDto.getTcKimlikNo())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Geçersiz TC Kimlik Numarası"));
        }
        
        // Check if TC Kimlik No already exists
        if (userService.findByTcKimlikNo(registrationDto.getTcKimlikNo()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bu TC Kimlik Numarası ile kayıtlı kullanıcı zaten mevcut"));
        }
        
        // Check if email already exists
        if (userService.findByEmail(registrationDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bu e-posta adresi ile kayıtlı kullanıcı zaten mevcut"));
        }
        
        // Create new user
        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setPassword(registrationDto.getPassword());
        newUser.setEmail(registrationDto.getEmail());
        newUser.setFirstName(registrationDto.getFirstName());
        newUser.setLastName(registrationDto.getLastName());
        newUser.setTcKimlikNo(registrationDto.getTcKimlikNo());
        newUser.setPhoneNumber(registrationDto.getPhoneNumber());
        newUser.setRole(User.Role.USER);
        newUser.setIsActive(true);
        
        User savedUser = userService.createUser(newUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", Map.of(
            "id", savedUser.getId(),
            "username", savedUser.getUsername(),
            "email", savedUser.getEmail(),
            "firstName", savedUser.getFirstName(),
            "lastName", savedUser.getLastName(),
            "fullName", savedUser.getFullName(),
            "tcKimlikNo", savedUser.getTcKimlikNo(),
            "musteriNo", savedUser.getMusteriNo()
        ));
        response.put("message", "Kayıt başarıyla tamamlandı");
        response.put("customerNumber", savedUser.getMusteriNo());
        
        return ResponseEntity.ok(response);
    }
    
}