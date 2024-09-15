package com.sampleProject.Sample.repository;

import com.sampleProject.Sample.entity.User;
import com.sampleProject.Sample.view.UserView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Page<User> findByStatusAndRole(byte status, byte role, Pageable pageable);

    Optional<User> findByUuid(UUID uuid);


}
