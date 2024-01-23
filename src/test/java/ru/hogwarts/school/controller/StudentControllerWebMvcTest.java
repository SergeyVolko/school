package ru.hogwarts.school.controller;

import net.minidev.json.JSONObject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.hogwarts.school.controller.StudentController;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;
import ru.hogwarts.school.service.StudentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StudentController.class)
public class StudentControllerWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private AvatarRepository avatarRepository;

    @SpyBean
    private StudentService studentService;
    @InjectMocks
    private StudentController studentController;

    @Test
    public void testGetStudentInfo() throws Exception {
        long id = 1;
        String name = "Bob";
        int age = 37;

        Student student = new Student(id, name, age);
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));

        mockMvc.perform(MockMvcRequestBuilders
                .get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.age").value(age));
    }

    @Test
    public void testCreateStudent() throws Exception {
        long id = 1;
        String name = "Bob";
        int age = 37;
        JSONObject studentObject = new JSONObject();
        studentObject.put("name", name);
        studentObject.put("age", age);
        Student student = new Student(id, name, age);

        when(studentRepository.save(any())).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/student")
                .content(studentObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.age").value(age));
    }

    @Test
    public void testEditStudent() throws Exception {
        long id = 1;
        String name = "Bob";
        int age = 37;
        int newAge = 40;
        JSONObject studentObject = new JSONObject();
        studentObject.put("id", id);
        studentObject.put("name", name);
        studentObject.put("age", newAge);

        Student curStudent = new Student(id, name, age);
        Student newStudent = new Student(id, name, newAge);

        when(studentRepository.findById(any(Long.class))).thenReturn(Optional.of(curStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(newStudent);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/student")
                        .content(studentObject.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.age").value(newAge));
    }

    @Test
    public void testDeleteStudent() throws Exception {
        long id = 1;
        String name = "Bob";
        int age = 37;
        Student student = new Student(id, name, age);

        when(studentRepository.findById(any())).thenReturn(Optional.of(student));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/student/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testFindStudents() throws Exception {
        int age = 23;
        long id1 = 1;
        long id2 = 2;
        String bob = "Bob";
        String john = "John";
        List<Student> students = List.of(
                new Student(id1, bob, age),
                new Student(id2, john, age)
        );

        when(studentRepository.findByAge(anyInt())).thenReturn(students);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student?age=23"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                        {"id":1,"name":"Bob","age":23,"faculty":null},
                        {"id":2,"name":"John","age":23,"faculty":null}
                        ]"""));
    }

    @Test
    public void testFindStudentByAgeBetween() throws Exception {
        int age1 = 23;
        int age2 = 24;
        int min = 20;
        int max = 25;
        long id1 = 1;
        long id2 = 2;
        String bob = "Bob";
        String john = "John";
        List<Student> students = List.of(
                new Student(id1, bob, age1),
                new Student(id2, john, age2)
        );

        when(studentRepository.findStudentByAgeBetween(anyInt(), anyInt())).thenReturn(students);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/findByAgeBetween?min="
                                + min + "&max=" + max))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                        {"id":1,"name":"Bob","age":23,"faculty":null},
                        {"id":2,"name":"John","age":24,"faculty":null}
                        ]"""));
    }

    @Test
    public void getStudentsOfFaculty() throws Exception {
        int age1 = 23;
        int age2 = 24;
        long id1 = 1;
        long id2 = 2;
        String bob = "Bob";
        String john = "John";
        List<Student> students = List.of(
                new Student(id1, bob, age1),
                new Student(id2, john, age2)
        );

        when(studentRepository.findStudentsByFaculty_Id(anyLong())).thenReturn(students);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/student/studentsOfFaculty/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        [
                        {"id":1,"name":"Bob","age":23,"faculty":null},
                        {"id":2,"name":"John","age":24,"faculty":null}
                        ]"""));
    }
    @Test
    public void testUploadAvatar() throws Exception {
        Student bob = new Student(1, "Bob", 37);

        when(studentRepository.findById(any())).thenReturn(Optional.of(bob));

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "avatar",
                Files.readAllBytes(Paths.get("data/kosmo.jpg"))
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/student/1/avatar")
                .file(mockMultipartFile)).andExpect(status().isOk());
    }

    @Test
    public void getAllStudentStartWithA() {
        Student artem = new Student(-1, "Artem", 34);
        Student andrey = new Student(-1, "Andrey", 34);
        Student abber = new Student(-1, "Abber", 22);
        List<Student> students = List.of(artem, andrey, abber);
        when(studentRepository.findAll()).thenReturn(students);
    }
}
