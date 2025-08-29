package com.mybankaccount.backend.service;

import com.mybankaccount.backend.entity.User;
import com.mybankaccount.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First try to find by username
        Optional<User> user = userRepository.findActiveUserByUsername(username);
        
        // If not found by username, try TC Kimlik No
        if (user.isEmpty()) {
            user = userRepository.findActiveUserByTcKimlikNo(username);
        }
        
        // If still not found, try Müşteri No
        if (user.isEmpty()) {
            user = userRepository.findActiveUserByMusteriNo(username);
        }
        
        return user.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
    
    public User createUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        // Check if TC Kimlik No already exists
        if (user.getTcKimlikNo() != null && userRepository.findByTcKimlikNo(user.getTcKimlikNo()).isPresent()) {
            throw new RuntimeException("TC Kimlik Numarası zaten kayıtlı: " + user.getTcKimlikNo());
        }
        
        // Generate unique customer number if not provided
        if (user.getMusteriNo() == null || user.getMusteriNo().isEmpty()) {
            user.setMusteriNo(generateUniqueCustomerNumber());
        } else if (userRepository.findByMusteriNo(user.getMusteriNo()).isPresent()) {
            throw new RuntimeException("Müşteri numarası zaten kayıtlı: " + user.getMusteriNo());
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(true);
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByTcKimlikNo(String tcKimlikNo) {
        return userRepository.findByTcKimlikNo(tcKimlikNo);
    }
    
    public Optional<User> findByMusteriNo(String musteriNo) {
        return userRepository.findByMusteriNo(musteriNo);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public List<User> findAllActiveUsers() {
        return userRepository.findAllActiveUsers();
    }
    
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Check if new username or email already exists (excluding current user)
        if (!user.getUsername().equals(userDetails.getUsername()) && 
            userRepository.existsByUsername(userDetails.getUsername())) {
            throw new RuntimeException("Username already exists: " + userDetails.getUsername());
        }
        
        if (!user.getEmail().equals(userDetails.getEmail()) && 
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDetails.getEmail());
        }
        
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    private String generateUniqueCustomerNumber() {
        String customerNumber;
        do {
            customerNumber = String.format("%011d", (long) (Math.random() * 100000000000L));
        } while (userRepository.findByMusteriNo(customerNumber).isPresent());
        return customerNumber;
    }
    
    public boolean isValidTcKimlikNo(String tcKimlikNo) {
        if (tcKimlikNo == null || tcKimlikNo.length() != 11) {
            return false;
        }
        
        // Check if all characters are digits
        if (!tcKimlikNo.matches("\\d{11}")) {
            return false;
        }
        
        // First digit cannot be 0
        if (tcKimlikNo.charAt(0) == '0') {
            return false;
        }
        
        // TC Kimlik No algorithm validation
        int[] digits = new int[11];
        for (int i = 0; i < 11; i++) {
            digits[i] = Character.getNumericValue(tcKimlikNo.charAt(i));
        }
        
        int oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        int evenSum = digits[1] + digits[3] + digits[5] + digits[7];
        
        int tenthDigit = ((oddSum * 7) - evenSum) % 10;
        int eleventhDigit = (oddSum + evenSum + digits[9]) % 10;
        
        return digits[9] == tenthDigit && digits[10] == eleventhDigit;
    }
    
    public boolean isValidCustomerNumber(String customerNumber) {
        return customerNumber != null && customerNumber.matches("\\d{11}");
    }
}