package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.util.Collection;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByAge(int age);

    Collection<Student> findStudentByAgeBetween(int min, int max);

    Student findStudentById(Long id);

    Collection<Student> findStudentsByFaculty_Id(Long id);

    @Query(value = "select count(*) from student", nativeQuery = true)
    int getAllStudents();

    @Query(value = "select round(avg(age)) from student", nativeQuery = true)
    int getAverageAgeOfStudents();

    @Query(value = "select * from student "
            + "order by id "
            + "offset (select count(*) from student) - 5",
            nativeQuery = true)
    List<Student> getFiveLastStudentById();

    @Query(value = "select * from student order by id limit 6",
    nativeQuery = true)
    List<Student> getSixFirstStudent();
}
