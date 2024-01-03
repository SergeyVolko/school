package ru.hogwarts.school.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private StudentController studentController;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    AvatarRepository avatarRepository;

    @Autowired
    FacultyRepository facultyRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    public void cleanUp() {
        studentRepository.deleteAll();
        avatarRepository.deleteAll();
        facultyRepository.deleteAll();
    }

    @Test
    void contextLoads() throws Exception {
        Assertions.assertThat(studentController).isNotNull();
    }

    @Test
    void testGetStudentInfo() throws Exception {
        Student student = new Student();
        student.setName("Test name");
        studentRepository.save(student);
        Assertions
                .assertThat(this.restTemplate.getForObject("/student/" + student.getId(), Student.class))
                .isEqualTo(student);
    }

    @Test
    void testCreateStudent() {
        Student student = new Student();
        student.setName("Bob");
        studentRepository.save(student);
        Assertions
                .assertThat(this.restTemplate
                        .postForObject( "/student", student, Student.class))
                .isEqualTo(student);
    }

    @Test
    void testEditStudent() {
        Student oldStudent = new Student(1, "Артур", 23);
        studentRepository.save(oldStudent);
        Student newStudent = new Student(oldStudent.getId(), "Артур", 24);
        this.restTemplate.put("/student", newStudent);
        Assertions
                .assertThat(this.restTemplate
                        .getForObject("/student/" + newStudent.getId(), Student.class))
                .isEqualTo(newStudent);
    }
    @Test
    void testFindStudents() {
        Student bob = new Student(-1, "Bob", 37);
        Student john = new Student(-1, "John", 37);
        studentRepository.save(bob);
        studentRepository.save(john);
        Assertions.assertThat(this.restTemplate
                        .getForObject("/student?age=37", Collection.class).size())
                .isEqualTo(2);
    }

    @Test
    void testDeleteStudent() {
        Student student = new Student(100, "John", 77);
        studentRepository.save(student);
        student = this.restTemplate
                .postForObject("/student", student, Student.class);
        restTemplate.delete("/student" + "/" + student.getId());
        Assertions.assertThat(this.restTemplate
                .getForObject("/student" + "/" + student.getId(), Student.class))
                .isEqualTo(new Student(0, null, 0));
    }

    @Test
    void testFindStudentByAgeBetween() {
        Student bob = new Student(-1, "Bob", 18);
        Student john = new Student(-1, "John", 19);
        studentRepository.save(bob);
        studentRepository.save(john);
        int size = restTemplate
                .getForObject("/student/findByAgeBetween?min=10&max=30", Collection.class)
                .size();
        Assertions.assertThat(size).isEqualTo(2);
    }

    @Test
    void testGetFacultyOfStudent() {
        Faculty faculty = new Faculty();
        faculty.setName("Математика");
        faculty.setColor("Синий");
        Student bob = new Student(-1, "Bob", 18);
        bob.setFaculty(faculty);
        facultyRepository.save(faculty);
        studentRepository.save(bob);
        Assertions.assertThat(restTemplate
                .getForObject("http://localhost:" + port + "/student/getFacultyOfStudent/" + bob.getId(), Faculty.class))
                .isNotNull();
    }

    @Test
    void testGetStudentsOfFaculty() {
        Faculty faculty = new Faculty();
        faculty.setName("Математика");
        faculty.setColor("Синий");
        Student bob = new Student(-1, "Bob", 18);
        bob.setFaculty(faculty);
        Student john = new Student(-1, "John", 21);
        john.setFaculty(faculty);
        facultyRepository.save(faculty);
        studentRepository.save(bob);
        studentRepository.save(john);
        int size = restTemplate
                .getForObject("http://localhost:"
                        + port
                        + "/student//studentsOfFaculty/" + faculty.getId(), Collection.class)
                .size();
        Assertions.assertThat(size).isEqualTo(2);
    }

    @Test
    public void testUploadAvatar() throws IOException {
        Student bob = new Student(-1, "Bob", 34);
        studentRepository.save(bob);
        byte[] avatar = Files.readAllBytes(Paths.get("src/main/resources/images/1.jpg"));
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", new ClassPathResource("1.jpg"));
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                RequestEntity.post("/student/{id}/avatar", bob.getId())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(body),
                String.class
        );
        System.out.println(responseEntity);
        /*Student student = new Student(0, "Bob", 34);
        studentRepository.save(student);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(MediaType.MULTIPART_FORM_DATA_VALUE));

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource("C:\\Users\\Lenovo\\IdeaProjects\\school\\src\\main\\resources\\images\\1.jpg"));

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);

        String serverUrl = "/student/"
                + student.getId()
                + "/avatar";

        ResponseEntity<String> response = restTemplate
                .postForEntity(serverUrl, requestEntity, String.class);
        System.out.println(response);*/
    }

    @Test
    public void uploadAvatar() {
       String result = restTemplate.postForObject("http://localhost:" + port + "/student/1/avatar",
                       new byte[1024], String.class);

        System.out.println(result);
    }
}
