package ru.hogwarts.school.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacultyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FacultyController facultyController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @AfterEach
    private void delete() {
        studentRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    void contextLoads() throws Exception {
        Assertions.assertThat(facultyController).isNotNull();
    }
    @Test
    void getFacultyInfo() {
        Faculty expect = new Faculty(1, "Математика", "Синий");
        facultyRepository.save(expect);
        Faculty faculty = restTemplate
                .getForObject("http://localhost:" + port + "/faculty/" + expect.getId(),
                        Faculty.class);
        System.out.println(faculty);
        Assertions.assertThat(faculty).isEqualTo(expect);
    }

    @Test
    void createFaculty() {
        Faculty expect = new Faculty(100, "Тест", "Синий");
        Faculty faculty =
                restTemplate.postForObject("http://localhost:" + port + "/faculty", expect, Faculty.class);
        Assertions.assertThat(faculty).isNotNull();
    }

    @Test
    void editFaculty() {
        Faculty curFaculty = new Faculty(102, "Тест", "Синий");
        facultyRepository.save(curFaculty);
        Faculty editFaculty = new Faculty(102, "Изменен тест", "Желтый");
        editFaculty.setId(curFaculty.getId());
        restTemplate.put("http://localhost:" + port + "/faculty", editFaculty);
        Faculty actual =
                restTemplate.getForObject("http://localhost:" + port + "/faculty/"
                        + editFaculty.getId(), Faculty.class);
        Assertions.assertThat(actual).isEqualTo(editFaculty);
    }

    @Test
    void deleteFaculty() {
        Faculty deleteFaculty = new Faculty(123, "Delete", "Red");
        facultyRepository.save(deleteFaculty);
        restTemplate.delete("http://localhost:" + port + "/faculty/" + deleteFaculty.getId());
        Assertions.assertThat(this.restTemplate
                        .getForObject("http://localhost:" + port + "/faculty" + "/"
                                + deleteFaculty.getId(), Faculty.class))
                .isEqualTo(new Faculty(0, null, null));
    }

    @Test
    void findFaculties() {
        Faculty faculty1 = new Faculty(-1, "Математика", "Синий");
        Faculty faculty2 = new Faculty(-1, "Русский", "Синий");
        facultyRepository.save(faculty1);
        facultyRepository.save(faculty2);
        Assertions.assertThat(restTemplate
                        .getForObject("http://localhost:" + port + "/faculty?color=Синий", Collection.class).size())
                .isEqualTo(2);
    }

    @Test
    void findFacultyByColorOrName() {
        Faculty faculty = new Faculty(-1, "Математика", "Синий");
        facultyRepository.save(faculty);
        Faculty actual =
               restTemplate.getForObject("http://localhost:"
                       + port
                       + "/faculty/findByNameOrColor?name=Математика", Faculty.class);
        Assertions.assertThat(actual).isEqualTo(faculty);
    }

    @Test
    void getFacultyOfStudent() {
        Faculty faculty = new Faculty(-1, "Математика", "Синий");
        facultyRepository.save(faculty);
        Student student = new Student(-1, "Bob", 23);
        student.setFaculty(faculty);
        studentRepository.save(student);
        Faculty actual = restTemplate.getForObject("http://localhost:"
                + port
                + "/faculty/getFacultyOfStudent/" + student.getId(), Faculty.class);
        Assertions.assertThat(actual).isEqualTo(faculty);
    }
}