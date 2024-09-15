package com.sampleProject.Sample.repository;


import com.sampleProject.Sample.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByEmail() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByEmail(user.getEmail());

        // then
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
    }


    @Test
    public void testFindByUuid() {
        // given
        User user = new User();
        user.setUuid(UUID.randomUUID());
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByUuid(user.getUuid());

        // then
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    public void testFindByStatusAndRole() {
        // given
        User user1 = new User();
        user1.setStatus((byte) 1);
        user1.setRole((byte) 1);
        entityManager.persist(user1);

        User user2 = new User();
        user2.setStatus((byte) 1);
        user2.setRole((byte) 2);
        entityManager.persist(user2);

        User user3 = new User();
        user3.setStatus((byte) 2);
        user3.setRole((byte) 1);
        entityManager.persist(user3);

        entityManager.flush();

        // when
        Pageable pageable = PageRequest.of(0, 5);
        Page<User> found = userRepository.findByStatusAndRole((byte) 1, (byte) 1, pageable);

        // then
        assertThat(found.getContent()).contains(user1);
        assertThat(found.getContent()).doesNotContain(user2, user3);
    }
}
