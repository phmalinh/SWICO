package com.swico.swico.repository;

import com.swico.swico.entity.User;
import com.swico.swico.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByLineCode(String lineCode);
    List<User> findByRoleAndActiveTrueOrderByFullNameAscUsernameAsc(Role role);
    List<User> findByActiveTrueOrderByFullNameAscUsernameAsc();
}
