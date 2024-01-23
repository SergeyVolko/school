package ru.hogwarts.school.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

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
        avatarRepository.deleteAll();
        studentRepository.deleteAll();
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
    public void testUploadAvatar() {
        Student bob = new Student(-1, "Bob", 34);
        studentRepository.save(bob);
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", new ClassPathResource("kosmo.jpg"));
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                RequestEntity.post("/student/{id}/avatar", bob.getId())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(body),
                String.class
        );
        System.out.println(responseEntity);
    }

    @Test
    public void downloadAvatar() {
        Student bob = new Student(-1, "Bob", 34);
        studentRepository.save(bob);
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("avatar", new ClassPathResource("kosmo.jpg"));
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                RequestEntity.post("/student/{id}/avatar", bob.getId())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(body),
                String.class
        );
        System.out.println(responseEntity);
        RequestEntity<Void> request = RequestEntity.get("/student/{id}/avatar/preview", bob.getId()).build();
        ResponseEntity<byte[]> response = restTemplate.exchange(
                request,
                byte[].class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getAllStudents() {
        studentRepository.save(new Student(-1, "Bob", 23));
        studentRepository.save(new Student(-1, "Jim", 21));
        studentRepository.save(new Student(-1, "Varg", 22));
        int count = restTemplate.getForObject("/student/count", Integer.class);
        Assertions.assertThat(count).isEqualTo(3);
    }

    @Test
    public void getAverageAgeOfStudents() {
        studentRepository.save(new Student(-1, "Bob", 21));
        studentRepository.save(new Student(-1, "Jim", 21));
        studentRepository.save(new Student(-1, "Varg", 21));
        int avgAge = restTemplate.getForObject("/student/avgAge", Integer.class);
        Assertions.assertThat(avgAge).isEqualTo(21);
    }

    @Test
    public void getLastFiveStudents() {
        Student jim = new Student(-1, "Jim", 21);
        studentRepository.save(new Student(-1, "Bob", 21));
        studentRepository.save(jim);
        studentRepository.save(new Student(-1, "Varg", 21));
        studentRepository.save(new Student(-1, "AAA", 21));
        studentRepository.save(new Student(-1, "BBB", 21));
        studentRepository.save(new Student(-1, "BBB", 21));
        List<Student> students = restTemplate.getForObject("/student/lastFive", List.class);
        System.out.println(students.get(0));
        System.out.println(jim.getId());
    }

    @Test
    public void getAverageAgeOfStudentsStream() {
        studentRepository.save(new Student(-1, "Bob", 21));
        studentRepository.save(new Student(-1, "Jim", 21));
        studentRepository.save(new Student(-1, "Varg", 21));
        int avgAge = restTemplate.getForObject("/student/streamAvgAge", Integer.class);
        Assertions.assertThat(avgAge).isEqualTo(21);
    }

    @Test
    public void getAllStudentStartWithA() {
        Student artem = new Student(-1, "Artem", 34);
        Student andrey = new Student(-1, "Andrey", 34);
        Student abber = new Student(-1, "Abber", 22);
        studentRepository.save(artem);
        studentRepository.save(andrey);
        studentRepository.save(abber);
        RequestEntity<Void> requestEntity = RequestEntity.get("/student/withA").build();
        ResponseEntity<List> responseEntity = restTemplate.exchange(requestEntity, List.class);
        List<String> result = responseEntity.getBody();
        Assertions.assertThat(result.get(0)).isEqualTo("ABBER");
    }
}
