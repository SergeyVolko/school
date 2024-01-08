package ru.hogwarts.school.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacultyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FacultyController facultyController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() throws Exception {
        Assertions.assertThat(facultyController).isNotNull();
    }
    @Test
    void getFacultyInfo() {
        Faculty faculty = restTemplate
                .getForObject("http://localhost:" + port + "/faculty/1", Faculty.class);
        System.out.println(faculty);
        Faculty expect = new Faculty(1, "Математика", "Синий");
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
        Faculty editFaculty = new Faculty(102, "Изменен тест", "Желтый");
        restTemplate.put("http://localhost:" + port + "/faculty", editFaculty);
        Faculty actual =
                restTemplate.getForObject("http://localhost:" + port + "/faculty/102", Faculty.class);
        Assertions.assertThat(actual).isEqualTo(editFaculty);
        restTemplate.put("http://localhost:" + port + "/faculty", curFaculty);
    }

    @Test
    void deleteFaculty() {
        Faculty deleteFaculty = new Faculty(123, "Delete", "Red");
        Faculty faculty =
                restTemplate.postForObject("http://localhost:" + port + "/faculty", deleteFaculty, Faculty.class);
        restTemplate.delete("http://localhost:" + port + "/faculty/" + faculty.getId());
        Assertions.assertThat(this.restTemplate
                        .getForObject("http://localhost:" + port + "/faculty" + "/" + faculty.getId(), Faculty.class))
                .isEqualTo(new Faculty(0, null, null));
    }

    @Test
    void findFaculties() {
        Assertions.assertThat(restTemplate
                        .getForObject("http://localhost:" + port + "/faculty?color=Синий", Collection.class).size())
                .isEqualTo(2);
    }

    @Test
    void findFacultyByColorOrName() {
       Faculty faculty =
               restTemplate.getForObject("http://localhost:"
                       + port
                       + "/faculty/findByNameOrColor?name=Математика", Faculty.class);
        Assertions.assertThat(faculty).isEqualTo(new Faculty(1, "Математика", "Синий"));
    }

    @Test
    void getFacultyOfStudent() {
        Faculty faculty = restTemplate.getForObject("http://localhost:"
                + port
                + "/faculty/getFacultyOfStudent/3", Faculty.class);
        Assertions.assertThat(faculty).isEqualTo(new Faculty(1, "Математика", "Синий"));
    }
}