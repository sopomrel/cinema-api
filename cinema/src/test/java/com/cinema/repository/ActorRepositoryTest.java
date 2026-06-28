package com.cinema.repository;

import com.cinema.entity.Actor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ActorRepository JPA tests")
class ActorRepositoryTest {

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Actor actor;

    @BeforeEach
    void setUp() {
        actor = Actor.builder()
                .firstName("Leonardo")
                .lastName("DiCaprio")
                .email("leo@example.com")
                .nationality("American")
                .build();
    }

    @Test
    @DisplayName("save persists actor and assigns generated id")
    void save_persistsActor() {
        Actor saved = actorRepository.save(actor);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("leo@example.com");
    }

    @Test
    @DisplayName("findById returns actor when present")
    void findById_returnsActor() {
        Actor saved = entityManager.persist(actor);
        entityManager.flush();

        Optional<Actor> found = actorRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Leonardo");
    }

    @Test
    @DisplayName("existsByEmail returns true for existing email")
    void existsByEmail_returnsTrueWhenPresent() {
        entityManager.persist(actor);
        entityManager.flush();

        assertThat(actorRepository.existsByEmail("leo@example.com")).isTrue();
        assertThat(actorRepository.existsByEmail("missing@example.com")).isFalse();
    }

    @Test
    @DisplayName("existsByEmailAndIdNot detects duplicate on another record")
    void existsByEmailAndIdNot_detectsDuplicate() {
        Actor first = entityManager.persist(Actor.builder()
                .firstName("Leonardo")
                .lastName("DiCaprio")
                .email("leo@example.com")
                .build());
        entityManager.persist(Actor.builder()
                .firstName("Brad")
                .lastName("Pitt")
                .email("brad@example.com")
                .build());
        entityManager.flush();

        assertThat(actorRepository.existsByEmailAndIdNot("leo@example.com", 999L)).isTrue();
        assertThat(actorRepository.existsByEmailAndIdNot("leo@example.com", first.getId())).isFalse();
    }
}
