-- liquibase formatted sql

-- changeset sergey:1
CREATE INDEX student_name ON student(name)

-- changeset sergey:2
CREATE INDEX faculty_name_color ON faculty(name, color)