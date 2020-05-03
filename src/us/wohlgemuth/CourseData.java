package us.wohlgemuth;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CourseData implements Serializable {

    private String term;
    HashMap<String, Crn> courseDetails;

    static public class Crn implements Serializable {
        private String raw;
        private String secType;
        private String crn;
        private String course;
        private String title;
        private String credit;
        private String maxEnrl;
        private String enrl;
        private String avail;
        private String waitList;
        private String days;
        private String start;
        private String end;
        private String bldg;
        private String room;
        private String instructor;

        public Crn(String raw) {
            this.raw = raw;
            parseRaw(raw);
        }

        public String getRaw() {
            return this.raw;
        }

        public String getCrn() {
            return crn;
        }

        public String getCourse() {
            return this.course;
        }

        public String getTitle() {
            return this.title;
        }

        public String getInstructor() {
            return this.instructor;
        }

        private void parseRaw(String raw) {
            secType = raw.substring(0, 4).trim();
            crn = raw.substring(5, 11).trim();
            course = raw.substring(12, 22).trim();
            title = raw.substring(23, 53).trim();
            credit = raw.substring(54, 60).trim();
            maxEnrl = raw.substring(61, 65).trim();
            enrl = raw.substring(66, 70).trim();
            avail = raw.substring(71, 79).trim();
            waitList = raw.substring(80, 84).trim();
            days = raw.substring(85, 92).trim();
            start = raw.substring(93, 100).trim();
            end = raw.substring(101, 108).trim();
            bldg = raw.substring(109, 114).trim();
            room = raw.substring(115, 125).trim();
            instructor = raw.substring(126).trim();
        }
    }

    static public class keyChange {
        public String key;
        public String value1;
        public String value2;

        public keyChange(String key, String value1, String value2) {
            this.key = key;
            this.value1 = value1;
            this.value2 = value2;
        }
    }

    public static ArrayList<keyChange> findChanges(Crn crn1, Crn crn2) {
        ArrayList<keyChange> changes = new ArrayList<>();
        if (crn1.secType.compareTo(crn2.secType) != 0)
            changes.add(new keyChange("Sec Type", crn1.secType, crn2.secType));
        if (crn1.crn.compareTo(crn2.crn) != 0) changes.add(new keyChange("CRN", crn1.crn, crn2.crn));
        if (crn1.course.compareTo(crn2.course) != 0) changes.add(new keyChange("Course", crn1.course, crn2.course));
        if (crn1.title.compareTo(crn2.title) != 0) changes.add(new keyChange("Title", crn1.title, crn2.title));
        if (crn1.credit.compareTo(crn2.credit) != 0) changes.add(new keyChange("Credit", crn1.credit, crn2.credit));
        if (crn1.maxEnrl.compareTo(crn2.maxEnrl) != 0)
            changes.add(new keyChange("Max Enrl", crn1.maxEnrl, crn2.maxEnrl));
        if (crn1.enrl.compareTo(crn2.enrl) != 0) changes.add(new keyChange("Enrl", crn1.enrl, crn2.enrl));
        if (crn1.avail.compareTo(crn2.avail) != 0) changes.add(new keyChange("Avail", crn1.avail, crn2.avail));
        if (crn1.waitList.compareTo(crn2.waitList) != 0)
            changes.add(new keyChange("Wait List", crn1.waitList, crn2.waitList));
        if (crn1.days.compareTo(crn2.days) != 0) changes.add(new keyChange("Days", crn1.days, crn2.days));
        if (crn1.start.compareTo(crn2.start) != 0) changes.add(new keyChange("Start", crn1.start, crn2.start));
        if (crn1.end.compareTo(crn2.end) != 0) changes.add(new keyChange("End", crn1.end, crn2.end));
        if (crn1.bldg.compareTo(crn2.bldg) != 0) changes.add(new keyChange("Bldg", crn1.bldg, crn2.bldg));
        if (crn1.room.compareTo(crn2.room) != 0) changes.add(new keyChange("Room", crn1.room, crn2.room));
        if (crn1.instructor.compareTo(crn2.instructor) != 0)
            changes.add(new keyChange("Instructor", crn1.instructor, crn2.instructor));
        return changes;
    }

    public CourseData(String term, HashMap<String, Crn> courseDetails) {
        this.term = term;
        this.courseDetails = courseDetails;
    }

    public String getTerm() {
        return term;
    }

    public Crn getCourseDetail(String crn) {
        if (null == courseDetails) return null;
        if (null == crn) return null;
        if (courseDetails.containsKey(crn)) {
            return courseDetails.get(crn);
        }
        return null;
    }

    private HashMap<String, Integer> getCourseDetailsHashCodes() {
        HashMap<String, Integer> hashCodes = new HashMap<>();
        if (null != courseDetails) {
            for (String id : courseDetails.keySet()) {
                hashCodes.put(id, courseDetails.get(id).getRaw().hashCode());
            }
        }
        return hashCodes;
    }

    public HashMap<String, Crn> getCourseDetails() {
        if (null == courseDetails) return null;
        return courseDetails;
    }

    public static HashSet<String> getChangeSet(CourseData courseData1, CourseData courseData2) {
        HashSet<String> changeSet = new HashSet<>();
        HashMap<String, Integer> hashCodes1;
        HashMap<String, Integer> hashCodes2;
        if (null != courseData1) {
            hashCodes1 = courseData1.getCourseDetailsHashCodes();
        } else {
            hashCodes1 = new HashMap<>();
        }
        if (null != courseData2) {
            hashCodes2 = courseData2.getCourseDetailsHashCodes();
        } else {
            hashCodes2 = new HashMap<>();
        }
        for (String crn : hashCodes1.keySet()) {
            if (changeSet.contains(crn)) continue;
            if (hashCodes2.containsKey(crn)) {
                if (hashCodes1.get(crn).intValue() != hashCodes2.get(crn).intValue()) {
                    changeSet.add(crn);
                }
            } else {
                changeSet.add(crn);
            }
        }
        for (String crn : hashCodes2.keySet()) {
            if (changeSet.contains(crn)) continue;
            if (hashCodes1.containsKey(crn)) {
                if (hashCodes1.get(crn).intValue() != hashCodes2.get(crn).intValue()) {
                    changeSet.add(crn);
                }
            } else {
                changeSet.add(crn);
            }
        }
        return changeSet;
    }

    public static CourseData deserialize(String filename) {
        CourseData courseData;
        try {
            FileInputStream fileInputStream = new FileInputStream(filename);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            courseData = (CourseData) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (ClassNotFoundException | IOException e) {
            return null;
        }
        return courseData;
    }

    public boolean serialize(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                File dirs = new File(file.getParent());
                dirs.mkdirs();
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
