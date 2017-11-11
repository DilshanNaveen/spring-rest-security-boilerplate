package com.springrestsecurityboilerplate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springrestsecurityboilerplate.user.User;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

  VerificationToken findByToken(String token);

  VerificationToken findByUser(User user);
}
