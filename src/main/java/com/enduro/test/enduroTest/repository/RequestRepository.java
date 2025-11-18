package com.enduro.test.enduroTest.repository;

import com.enduro.test.enduroTest.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByCompleted(boolean completed);
}
