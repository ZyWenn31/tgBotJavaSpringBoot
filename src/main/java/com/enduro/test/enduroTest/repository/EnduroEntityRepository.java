package com.enduro.test.enduroTest.repository;

import com.enduro.test.enduroTest.model.EnduroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnduroEntityRepository extends JpaRepository<EnduroEntity, Long> {
    Optional<EnduroEntity> findByName(String name);
}
