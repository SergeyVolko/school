SELECT
    s.name,
    age,
    f.name
FROM student s LEFT JOIN faculty f on s.faculty_id = f.id;

SELECT
    name,
    student_id
FROM student LEFT JOIN avatar a on student.id = a.student_id
WHERE student_id IS NOT NULL;