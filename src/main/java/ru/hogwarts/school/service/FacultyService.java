package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;
import java.util.Collection;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final Logger logger = LoggerFactory.getLogger(FacultyService.class);
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty addFaculty(Faculty faculty) {
        logger.debug("Добавили факультет");
        return facultyRepository.save(faculty);
    }

    public Faculty findFaculty(Long id) {
        logger.info("Поиск факультета");
        return facultyRepository.findById(id).get();
    }

    public Faculty editFaculty(Faculty faculty) {
        if (facultyRepository.findById(faculty.getId()).isEmpty()) {
            logger.warn("Факультет {} не найден", faculty);
            return null;
        }
        return facultyRepository.save(faculty);
    }

    public Faculty deleteFaculty(long id) {
        logger.info("Удаление факультета по id: {}", id);
        Faculty faculty = findFaculty(id);
        if (faculty != null) {
            facultyRepository.deleteById(id);
        }
        return faculty;
    }

    public Collection<Faculty> findByColor(String color) {
        logger.info("Получение списка факультетов по color: {}",  color);
        return facultyRepository.findByColor(color);
    }

    public Faculty findFirstFacultyByNameIgnoreCase(String name) {
        logger.info("Получение факультета по имени {} с игнорированием регистра", name);
        return facultyRepository.findFirstFacultyByNameIgnoreCase(name);
    }

    public Faculty findFirstFacultyByColorIgnoreCase(String color) {
        logger.info("Получение факультета по цвету {} с игнорированием регистра", color);
        return facultyRepository.findFirstFacultyByColorIgnoreCase(color);
    }

    public Faculty findFacultyByStudentId(Long id) {
        logger.info("Поиск факультета по id студента {} c использованием запроса", id);
        return facultyRepository.findFacultyByStudentId(id);
    }
}
