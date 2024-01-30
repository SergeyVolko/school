package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@Transactional
public class StudentService {

    private static final int MAX_STUDENTS = 6;
    private static final String PREFIX = "A";
    private final Logger logger = LoggerFactory.getLogger(StudentService.class);

    @Value("${avatars.dir.path}")
    private String avatarsDir;
    private final StudentRepository studentRepository;
    private final AvatarRepository avatarRepository;

    private LinkedList<Student> students;

    public StudentService(StudentRepository studentRepository, AvatarRepository avatarRepository) {
        this.studentRepository = studentRepository;
        this.avatarRepository = avatarRepository;
    }

    public Student addStudent(Student student) {
        logger.debug("Добавили студента");
        return studentRepository.save(student);
    }

    public Student findStudent(long id) {
        logger.debug("Поиск студента");
        return studentRepository.findById(id).get();
    }

    public Student editStudent(Student student) {
        logger.info("Редактирование данных студента: {}", student);
        if (studentRepository.findById(student.getId()).isEmpty()) {
            logger.warn("Студенте {} не найден.", student);
            return null;
        }
        return studentRepository.save(student);
    }

    public Student deleteStudent(long id) {
        logger.info("Удление студента по id: {}", id);
        Student student = findStudent(id);
        if (student != null) {
            studentRepository.deleteById(id);
        }
        return student;
    }

    public Collection<Student> findByAge(int age) {
        logger.info("Поиск студентов по age: {}", age);
        return studentRepository.findByAge(age);
    }

    public Collection<Student> findStudentByAgeBetween(int min, int max) {
        logger.info("Поиск студентов в диапазоне возрастов от {} до {}", min, max);
        return studentRepository.findStudentByAgeBetween(min, max);
    }

    public Faculty getFacultyOfStudent(Long id) {
        logger.info("Поиск факультета по id: {} студента.", id);
        Student student = studentRepository.findStudentById(id);
        if (student == null) {
            return null;
        }
        return student.getFaculty();
    }

    public Collection<Student> getStudentsOfFaculty(Long id) {
        logger.info("Поиск студентов по id факультета: {}", id);
        return studentRepository.findStudentsByFaculty_Id(id);
    }

    public Avatar findAvatar(long studentId) {
        logger.info("Поиск аватара по id студента: " + studentId);
        return avatarRepository.findAvatarByStudent_Id(studentId).orElseThrow(() -> {
            logger.error("Аватар с id: {} не найден", studentId);
            return new NoSuchElementException("Аватар не найден.");
        });
    }

    public void uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        logger.info("Загрузка аватара по id студента: {}", studentId);
        Student student = studentRepository.findStudentById(studentId);
        Path filePath = Path.of(avatarsDir, studentId + "." + getExtension(file.getOriginalFilename()));
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);
        try (InputStream is = file.getInputStream();
             OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
             BufferedInputStream bis = new BufferedInputStream(is, 1024);
             BufferedOutputStream bos = new BufferedOutputStream(os, 1024)){
            bis.transferTo(bos);
        }
        Avatar avatar = avatarRepository.findAvatarByStudent_Id(studentId).orElseGet(Avatar::new);
        avatar.setStudent(student);
        avatar.setFilePath(filePath.toString());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(generateImagePreview(filePath));
        avatarRepository.save(avatar);
    }

    public int getAllStudents() {
        logger.info("Получение всех студентов");
        return studentRepository.getAllStudents();
    }

    public int getAverageAgeOfStudents() {
        logger.info("Получение среднего возраста всех студентов");
        return studentRepository.getAverageAgeOfStudents();
    }

    public List<Student> getFiveLastStudentById() {
        if (getAllStudents() < 5) {
            String message = "В базе не достаточно студентов.";
            logger.error(message);
            throw new UnsupportedOperationException(message);
        }
        return studentRepository.getFiveLastStudentById();
    }

    public List<String> getAllStudentStartWithA() {
        logger.info("Получение отсортированного списка имен студентов начинающихся с {}",
                PREFIX);
        return studentRepository.findAll()
                .stream()
                .filter(s -> s.getName().startsWith(PREFIX))
                .map(s -> s.getName().toUpperCase())
                .sorted(String::compareTo)
                .toList();
    }

    public int getAverageAgeOfStudentsFromStream() {
        logger.info("Получение среднего возраста всех студентов");
        return (int) studentRepository.findAll().stream()
                .mapToInt(Student::getAge)
                .average().orElse(0);
    }

    public void printFirstSixStudentsParallel(Consumer<Integer> consumer) {
        validateStudents();
        consumer.accept(0);
        consumer.accept(1);

        new Thread(() -> {
            consumer.accept(2);
            consumer.accept(3);
        }).start();

        new Thread(() -> {
            consumer.accept(4);
            consumer.accept(5);
        }).start();
    }

    public synchronized void printStudentNameSynchronized() {
        System.out.println(students.pollFirst());
    }

    public void printNameStudent(int index) {
        System.out.println(students.get(index));
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private byte[] generateImagePreview(Path filePath) throws IOException {
        try (InputStream is = Files.newInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(is, 1024);
        ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            BufferedImage image = ImageIO.read(bis);
            int height = image.getHeight() / (image.getWidth() / 100);
            BufferedImage preview = new BufferedImage(100, height, image.getType());
            Graphics2D graphics = preview.createGraphics();
            graphics.drawImage(image, 0, 0, 100, height, null);
            graphics.dispose();
            ImageIO.write(preview, getExtension(filePath.getFileName().toString()), baos);
            return baos.toByteArray();
        }
    }

    private void validateStudents() {
        students = new LinkedList<>(studentRepository.getSixFirstStudent());
        System.out.println(students);
        if(students.size() < MAX_STUDENTS) {
            throw new NoSuchElementException("В институте недостаточное количество студентов");
        }
    }
}
