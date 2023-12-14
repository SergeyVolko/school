package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@Service
public class StudentService {
    private final HashMap<Long, Student> students = new HashMap<>();
    private final StudentRepository studentRepository;
    private long count = 0;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student addStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student findStudent(long id) {
        return studentRepository.findById(id).get();
    }

    public Student editStudent(Student student) {
        if (studentRepository.findById(student.getId()).isEmpty()) {
            return null;
        }
        return studentRepository.save(student);
    }

    public Student deleteStudent(long id) {
        Student student = findStudent(id);
        if (student != null) {
            studentRepository.deleteById(id);
        }
        return student;
    }

    public Collection<Student> findByAge(int age) {
        return studentRepository.findByAge(age);
    }

    public Collection<Student> findStudentByAgeBetween(int min, int max) {
        return studentRepository.findStudentByAgeBetween(min, max);
    }

    public Faculty getFacultyOfStudent(Long id) {
        Student student = studentRepository.findStudentById(id);
        if (student == null) {
            return null;
        }
        return studentRepository.findStudentById(id).getFaculty();
    }

    public Collection<Student> getStudentsOfFaculty(Long id) {
        return studentRepository.findStudentsByFaculty_Id(id);
    }
}
