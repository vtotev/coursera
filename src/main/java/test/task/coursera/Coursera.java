package test.task.coursera;

import test.task.coursera.entity.CourseData;
import test.task.coursera.entity.StudentData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;


public class Coursera {

    static final Connection conn;

    static {
        Properties props = new Properties();
        props.put("user", "local");
        props.put("password", "");
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/coursera?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    props);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static List<StudentData> studentData = new ArrayList<>();

    public static void main(String[] args) throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter student pins, separated by comma: ");
        String pins = scan.nextLine().trim().replaceAll(",\\s*", ", ");
        System.out.print("Enter Start Date (yyyy-MM-dd): ");
        LocalDate startDate = LocalDate.parse(scan.nextLine());
        System.out.print("Enter End Date (yyyy-MM-dd): ");
        LocalDate endDate = LocalDate.parse(scan.nextLine());
        System.out.print("Enter minimum credits needed: ");
        Integer minCredits = Integer.parseInt(scan.nextLine());
        System.out.print("Enter output path: ");
        String outputPath = scan.nextLine();
        System.out.print("Enter output format (csv/html/none): ");
        String outputFormat = scan.nextLine();
        Map<String, Integer> studentIDsWithNeededCredits = getStudentIDsWithNeededCredits(pins, minCredits, startDate, endDate);
        studentIDsWithNeededCredits.forEach((key, value) -> studentData.add(new StudentData(key, null, value, new ArrayList<>())));
        fillStudentsList(String.join(", ", studentIDsWithNeededCredits.keySet()), startDate, endDate);

        if (outputFormat.equalsIgnoreCase("csv")) {
            writeFile(outputPath, generateCSV(), "csv");
        } else if (outputFormat.equalsIgnoreCase("html")) {
            writeFile(outputPath, generateHTML(), "html");
        } else {
            writeFile(outputPath, generateCSV(), "csv");
            writeFile(outputPath, generateHTML(), "html");
        }
    }

    private static void writeFile(String outputPath, String data, String outFormat) {
        String fileName = outputPath;
        if (!fileName.endsWith(File.separator)) {
            fileName = fileName + File.separator;
        }
        fileName = fileName + "output." + outFormat;
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter(fileName));
            fw.write(data);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Student,Total credit,Course name,Time,Credit,Instructor").append(System.lineSeparator());
        studentData.forEach(s -> {
            s.getCourses().forEach(c -> {
                String format = String.format("%s,%d,%s,%d,%d,%s", s.getStudentName(), s.getTotalCredits(), c.getCourseName(), c.getTotalTime(), c.getCredits(), c.getInstructorName());
                sb.append(format).append(System.lineSeparator());
            });
        });
        return sb.toString();
    }

    public static String generateHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<style>\n" +

                "td, th {\n" +
                "  border: 1px solid black;\n" +
                "  text-align: left;\n" +
                "  padding: 8px;\n" +
                "}\n" +
                "</style>").append(System.lineSeparator());
        sb.append("<body>\n" +
                        "<table>\n" +
                        "  <tr style=\"background-color: #2874A6;\">\n" +
                        "    <th>Student name</th>\n" +
                        "    <th>Total credits</th>\n" +
                        "    <th></th>\n" +
                        "    <th></th>\n" +
                        "    <th></th>\n" +
                        "  </tr>\n" +
                        "  <tr style=\"background-color: #2874A6;\">\n" +
                        "    <th></th>\n" +
                        "    <th>Course name</th>\n" +
                        "    <th>Time</th>\n" +
                        "    <th>Credit</th>\n" +
                        "    <th>Instructor</th>\n" +
                        "  </tr>\n")
                .append(System.lineSeparator());

        String studentRow = "<tr style=\"background-color: #AED6F1;\">\n" +
                "    <td>%s</td>\n" +
                "    <td>%s</td>\n" +
                "    <td></td>\n" +
                "    <td></td>\n" +
                "    <td></td>\n" +
                "  </tr>\n";

        String courseRow = "<tr style=\"background-color: #ABEBC6;\">\n" +
                "    <td></td>\n" +
                "    <td>%s</td>\n" +
                "    <td>%d</td>\n" +
                "    <td>%d</td>\n" +
                "    <td>%s</td>\n" +
                "  </tr>\n";

        studentData.forEach(s -> {
            sb.append(String.format(studentRow, s.getStudentName(), s.getTotalCredits()));
            s.getCourses().forEach(c -> {
                sb.append(String.format(courseRow, c.getCourseName(), c.getTotalTime(), c.getCredits(), c.getInstructorName()));
            });
        });
        sb.append("</table>\n" +
                "</body>\n" +
                "</html>\n" +
                "\n");
        return sb.toString();
    }

    public static Map<String, Integer> getStudentIDsWithNeededCredits(String pins, Integer minCredits, LocalDate startDate, LocalDate endDate) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT a.pin, sum(c.credit) as credits " +
                "FROM students a \n" +
                "LEFT JOIN students_courses_xref b on a.pin = b.student_pin \n" +
                "LEFT JOIN courses c on c.id = b.course_id \n" +
                "WHERE b.completion_date IS NOT NULL AND b.completion_date BETWEEN ? and ? \n");
        if (!pins.isBlank()) {
            sb.append(" AND pin in ( " + pins + " )");
        }
        sb.append("GROUP BY a.pin \n" +
                "HAVING credits > ?");

        PreparedStatement ps = conn.prepareStatement(sb.toString());
        ps.setDate(1, Date.valueOf(startDate));
        ps.setDate(2, Date.valueOf(endDate));
        ps.setInt(3, minCredits);
        ResultSet rs = ps.executeQuery();
        Map<String, Integer> result = new HashMap<>();
        while (rs.next()) {
            result.put(rs.getString(1), rs.getInt(2));
        }
        return result;
    }

    public static void fillStudentsList(String pins, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "select a.pin, concat_ws(' ', a.first_name, a.last_name) as student_name, c.name as course_name, c.total_time, c.credit, concat_ws(' ', d.first_name, d.last_name) as instructor_name from students a\n" +
                "left join students_courses_xref b on a.pin = b.student_pin\n" +
                "left join courses c on c.id = b.course_id\n" +
                "left join instructors d on d.id = c.instructor_id\n" +
                "where b.completion_date is not null and b.completion_date between ? and ? and a.pin in ( " + pins + " ) ";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDate(1, Date.valueOf(startDate));
        ps.setDate(2, Date.valueOf(endDate));
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String pin = rs.getString("pin");
            String studentName = rs.getString("student_name");
            String courseName = rs.getString("course_name");
            int totalTime = rs.getInt("total_time");
            int credit = rs.getInt("credit");
            String instructorName = rs.getString("instructor_name");
            StudentData student = studentData.stream().filter(s -> s.getId().equals(pin)).findFirst().orElse(null);
            student.setStudentName(studentName);
            student.getCourses().add(new CourseData(courseName, totalTime, credit, instructorName));
        }
    }
}
