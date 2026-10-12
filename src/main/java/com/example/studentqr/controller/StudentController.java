package com.example.studentqr.controller;

import com.example.studentqr.model.Student;
import com.example.studentqr.repository.StudentRepository;
import com.example.studentqr.service.QrService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentRepository repository;
    private final QrService qrService;

    public StudentController(StudentRepository repository, QrService qrService) {
        this.repository = repository;
        this.qrService = qrService;
    }

    @GetMapping
    public List<Student> all(@RequestParam(required = false) String search) {
        List<Student> students = repository.findAll();
        if (search == null || search.isBlank()) return students;
        String q = search.toLowerCase();
        return students.stream().filter(s ->
            safe(s.getName()).contains(q) || safe(s.getRollNo()).contains(q) ||
            safe(s.getDepartment()).contains(q) || safe(s.getSection()).contains(q)
        ).toList();
    }

    @GetMapping("/{id}")
    public Student get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    @GetMapping("/roll/{rollNo}")
    public Student byRoll(@PathVariable String rollNo) {
        return repository.findByRollNoIgnoreCase(rollNo).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "No student found for roll number " + rollNo));
    }

    @PostMapping
    public Student create(@RequestBody Student student) {
        validate(student);
        if (repository.existsByRollNoIgnoreCase(student.getRollNo().trim()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll number already exists");
        student.setId(null);
        student.setName(student.getName().trim());
        student.setRollNo(student.getRollNo().trim());
        student.setDepartment(student.getDepartment().trim());
        return repository.save(student);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @RequestBody Student input) {
        Student current = get(id);
        validate(input);
        repository.findByRollNoIgnoreCase(input.getRollNo().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Roll number already exists");
        });
        current.setName(input.getName().trim());
        current.setRollNo(input.getRollNo().trim());
        current.setDepartment(input.getDepartment().trim());
        current.setSection(input.getSection());
        current.setCourse(input.getCourse());
        current.setContact(input.getContact());
        current.setEmail(input.getEmail());
        return repository.save(current);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Student student = get(id);
        repository.delete(student);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qr(@PathVariable Long id) {
        Student student = get(id);
        return qrService.createPng("STUDENT_ROLL:" + student.getRollNo());
    }

    private void validate(Student s) {
        if (s.getName() == null || s.getName().isBlank() ||
            s.getRollNo() == null || s.getRollNo().isBlank() ||
            s.getDepartment() == null || s.getDepartment().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Name, roll number, and department are required");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
