package ru.hogwarts.school.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.nio.file.Files;
import java.util.Collection;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentController studentController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() throws Exception {
        Assertions.assertThat(studentController).isNotNull();
    }

    @Test
    void testGetStudentInfo() throws Exception {
        Assertions
                .assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/student/1", Student.class))
                .isEqualTo(new Student(1, "Сергей", 34));
    }

    @Test
    void testCreateStudent() {
        Student student = new Student(100, "Bob", 33);
        Assertions
                .assertThat(this.restTemplate
                        .postForObject("http://localhost:" + port + "/student", student, Student.class))
                .isNotNull();
    }

    @Test
    void testEditStudent() {
        Student student = new Student(1, "Артур", 23);
        this.restTemplate.put("http://localhost:" + port + "/student", student);
        Assertions
                .assertThat(this.restTemplate
                        .getForObject("http://localhost:" + port + "/student/1", Student.class))
                .isEqualTo(student);
        Student oldStudent = new Student(1, "Сергей", 34);
        this.restTemplate.put("http://localhost:" + port + "/student", oldStudent);
    }
    @Test
    void testFindStudents() {
        Assertions.assertThat(this.restTemplate
                        .getForObject("http://localhost:" + port + "/student?age=37", Collection.class).size())
                .isEqualTo(2);
    }

    @Test
    void testDeleteStudent() {
        Student student = new Student(100, "John", 77);
        student = this.restTemplate
                .postForObject("http://localhost:" + port + "/student", student, Student.class);
        restTemplate.delete("http://localhost:" + port + "/student" + "/" + student.getId());
        Assertions.assertThat(this.restTemplate
                .getForObject("http://localhost:" + port + "/student" + "/" + student.getId(), Student.class))
                .isEqualTo(new Student(0, null, 0));
    }

    @Test
    void testFindStudentByAgeBetween() {
        int size = restTemplate
                .getForObject("http://localhost:"
                        + port
                        + "/student?age=15&age=20", Collection.class)
                .size();
        Assertions.assertThat(size).isEqualTo(1);
    }

    @Test
    void testGetFacultyOfStudent() {
        Assertions.assertThat(restTemplate
                .getForObject("http://localhost:" + port + "/student/getFacultyOfStudent/3", Faculty.class))
                .isNotNull();
    }

    @Test
    void testGetStudentsOfFaculty() {
        int size = restTemplate
                .getForObject("http://localhost:"
                        + port
                        + "/student//studentsOfFaculty/1", Collection.class)
                .size();
        Assertions.assertThat(size).isEqualTo(3);
    }

    @Test
    public void testUploadAvatar() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource("C:\\Users\\Lenovo\\IdeaProjects\\school\\src\\main\\resources\\images\\1.jpg"));

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        String serverUrl = "http://localhost:" + port + "/student/1/avatar";

        ResponseEntity<String> response = restTemplate
                .postForEntity(serverUrl, requestEntity, String.class);
        System.out.println(response);
    }
}
