package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.hogwarts.school.model.Faculty;
import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    List<Faculty> findByColor(String color);

    Faculty findFirstFacultyByNameIgnoreCase(String name);

    Faculty findFirstFacultyByColorIgnoreCase(String color);

    @Query("select f from Faculty f inner join Student s on f.id = s.faculty.id and s.id = :id")
    Faculty findFacultyByStudentId(@Param("id") Long id);
}
