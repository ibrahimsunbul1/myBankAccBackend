package com.mybankaccount.backend.config;

import com.mybankaccount.backend.entity.User;
import com.mybankaccount.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserService userService;
    
    @Override
    public void run(String... args) throws Exception {
        // Test kullanıcısı oluştur (eğer yoksa)
        if (!userService.findByUsername("testuser").isPresent()) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword("password123");
            testUser.setEmail("test@example.com");
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setPhoneNumber("+90 555 123 4567");
            testUser.setTcKimlikNo("12345678901"); // Valid test TC ID
            testUser.setMusteriNo("10000000001"); // Test customer number
            testUser.setRole(User.Role.USER);
            
            userService.createUser(testUser);
            System.out.println("Test kullanıcısı oluşturuldu:");
            System.out.println("Kullanıcı Adı: testuser");
            System.out.println("Şifre: password123");
            System.out.println("TC Kimlik No: 12345678901");
            System.out.println("Müşteri No: 10000000001");
        }
        
        // Admin kullanıcısı oluştur (eğer yoksa)
        if (!userService.findByUsername("admin").isPresent()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword("admin123");
            adminUser.setEmail("admin@example.com");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setPhoneNumber("+90 555 987 6543");
            adminUser.setTcKimlikNo("98765432109"); // Valid test TC ID
            adminUser.setMusteriNo("10000000002"); // Test customer number
            adminUser.setRole(User.Role.ADMIN);
            
            userService.createUser(adminUser);
            System.out.println("Admin kullanıcısı oluşturuldu:");
            System.out.println("Kullanıcı Adı: admin");
            System.out.println("Şifre: admin123");
            System.out.println("TC Kimlik No: 98765432109");
            System.out.println("Müşteri No: 10000000002");
        }
    }
}