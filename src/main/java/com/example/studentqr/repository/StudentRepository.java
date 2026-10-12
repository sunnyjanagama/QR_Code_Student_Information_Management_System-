package com.example.studentqr.repository;

import com.example.studentqr.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNoIgnoreCase(String rollNo);
    boolean existsByRollNoIgnoreCase(String rollNo);
}
