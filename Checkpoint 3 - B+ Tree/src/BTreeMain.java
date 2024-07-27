import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Main Application.
 */
public class BTreeMain {

    public static void main(String[] args) {

        /** Read the input file -- input.txt */
        Scanner scan = null;
        try {
            scan = new Scanner(new File("src/input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }

        /** Read the minimum degree of B+Tree first */

        int degree = scan.nextInt();

        BTree bTree = new BTree(degree);

        /** Reading the database student.csv into B+Tree Node*/
        List<Student> studentsDB = getStudents();

        for (Student s : studentsDB) {
            bTree.insert(s);
        }

        // DEBUG
        System.out.println(bTree.print());

        /** Start reading the operations now from input file*/
        Random random = new Random(); // New Random Object
        try {
            while (scan.hasNextLine()) {
                Scanner s2 = new Scanner(scan.nextLine());

                while (s2.hasNext()) {

                    String operation = s2.next();

                    switch (operation) {
                        case "insert": {

                            long studentId = Long.parseLong(s2.next());
                            String studentName = s2.next() + " " + s2.next();
                            String major = s2.next();
                            String level = s2.next();
                            int age = Integer.parseInt(s2.next());
                            /** TODO: Write a logic to generate recordID*/
                            // If student has recordID, set recordID, otherwise generate random recordID
                            long recordID = s2.hasNextLong() ? s2.nextLong() : random.nextLong();

                            Student s = new Student(studentId, age, studentName, major, level, recordID);
                            bTree.insert(s);
                            break;
                        }
                        case "delete": {
                            long studentId = Long.parseLong(s2.next());
                            boolean result = bTree.delete(studentId);
                            if (result)
                                System.out.println("Student deleted successfully.");
                            else
                                System.out.println("Student deletion failed.");

                            break;
                        }
                        case "search": {
                            long studentId = Long.parseLong(s2.next());
                            long recordID = bTree.search(studentId);
                            if (recordID != -1)
                                System.out.println("Student exists in the database at " + recordID);
                            else
                                System.out.println("Student does not exist.");
                            break;
                        }
                        case "print": {
                            List<Long> listOfRecordID = new ArrayList<>();
                            listOfRecordID = bTree.print();
                            System.out.println("List of recordIDs in B+Tree " + listOfRecordID.toString());
                        }
                        default:
                            System.out.println("Wrong Operation");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Student> getStudents() {

        /** TODO:
         * Extract the students information from "Students.csv"
         * return the list<Students>
         */

        List<Student> studentList = new ArrayList<>();

        /** Read the input file -- student.csv */
        try (Scanner scanner = new Scanner(new File("src/student.csv"))) {
            while (scanner.hasNextLine()) { // Parse each line into Student object and add to studentList
                String line = scanner.nextLine(); // Next line
                String[] info = line.split(","); // Split on commas
                // Student information
                long studentId = Long.parseLong(info[0]);
                int age = Integer.parseInt(info[4]);
                String studentName = info[1];
                String major = info[2];
                String level = info[3];
                long recordId = Long.parseLong(info[5]);
                // Create new student and add to studentList
                studentList.add(new Student(studentId, age, studentName, major, level, recordId));
            }
        } catch (FileNotFoundException e) { // Catch exception if student.csv not found
            System.out.println("File not found.");
        }


        return studentList;
    }
}
