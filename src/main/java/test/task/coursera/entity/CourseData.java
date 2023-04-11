package test.task.coursera.entity;

public class CourseData {
    private String courseName;
    private Integer totalTime;
    private Integer credits;
    private String instructorName;

    public CourseData(String courseName, Integer totalTime, Integer credits, String instructorName) {
        this.courseName = courseName;
        this.totalTime = totalTime;
        this.credits = credits;
        this.instructorName = instructorName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }
}