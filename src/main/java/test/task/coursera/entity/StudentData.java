package test.task.coursera.entity;

import test.task.coursera.Coursera;

import java.util.List;

public class StudentData {
    private String id;
    private String studentName;
    private Integer totalCredits;
    List<CourseData> courses;

    public StudentData(String id, String studentName, Integer totalCredits, List<CourseData> courses) {
        this.id = id;
        this.studentName = studentName;
        this.totalCredits = totalCredits;
        this.courses = courses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Integer getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Integer totalCredits) {
        this.totalCredits = totalCredits;
    }

    public List<CourseData> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseData> courses) {
        this.courses = courses;
    }
}