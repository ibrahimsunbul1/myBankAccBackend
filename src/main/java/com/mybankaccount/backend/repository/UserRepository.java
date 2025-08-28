package com.mybankaccount.backend.repository;

import com.mybankaccount.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActiveUsers();
    
    Optional<User> findByTcKimlikNo(String tcKimlikNo);
    
    Optional<User> findByMusteriNo(String musteriNo);
    
    @Query("SELECT u FROM User u WHERE u.tcKimlikNo = ?1 AND u.isActive = true")
    Optional<User> findActiveUserByTcKimlikNo(String tcKimlikNo);
    
    @Query("SELECT u FROM User u WHERE u.musteriNo = ?1 AND u.isActive = true")
    Optional<User> findActiveUserByMusteriNo(String musteriNo);
    
    boolean existsByTcKimlikNo(String tcKimlikNo);
    
    boolean existsByMusteriNo(String musteriNo);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
}