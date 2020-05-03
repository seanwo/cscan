package us.wohlgemuth;

import java.util.ArrayList;
import java.util.HashSet;

public class Monitor {
    private Configuration config;
    Notifier notifier;

    public Monitor(Configuration config) {
        this.config = config;
        notifier = new Notifier(config.getSmtpHost(), config.getSmtpUser(), config.getSmtpPassword());
    }

    private CourseData getCurrentCourseData() {
        SiteParser siteParser = new SiteParser(config.getTerm(), config.getCrns());
        return siteParser.getCourseData();
    }

    private CourseData getPreviousCourseData() {
        String filename = System.getProperty("user.home") + "/.cscan/data.ser";
        return CourseData.deserialize(filename);
    }

    private void serializeCourseData(CourseData courseData) {
        String filename = System.getProperty("user.home") + "/.cscan/data.ser";
        courseData.serialize(filename);
    }

    private void sendNotification(HashSet<String> changeSet, CourseData prevCourseData, CourseData currCourseData) {
        StringBuilder body = new StringBuilder();
        for (String crn : changeSet) {
            CourseData.Crn prev = prevCourseData.getCourseDetail(crn);
            CourseData.Crn curr = currCourseData.getCourseDetail(crn);
            ArrayList<CourseData.keyChange> changes = CourseData.findChanges(prev, curr);
            body.append("CRN: " + curr.getCrn() + " Course: " + curr.getCourse() + " Title: " + curr.getTitle() + " Instructor: " + curr.getInstructor() + "\n");
            for (CourseData.keyChange change : changes) {
                body.append(change.key + ": [" + change.value1 + " -> " + change.value2 + "]\n");
            }
            body.append("\n");
        }
        System.out.println(body.toString());
        String subject = "Course Change Detected (" + currCourseData.getTerm() + ")";
        notifier.sendEmail(subject, body.toString(), config.getEmailAddresses());
    }

    private boolean detectChanges(CourseData prevCourseData, CourseData currCourseData) {
        HashSet<String> changeSet = CourseData.getChangeSet(prevCourseData, currCourseData);
        if (changeSet.isEmpty()) {
            System.out.println("No changes detected.");
            return false;
        }
        System.out.println("Changes detected!");
        sendNotification(changeSet, prevCourseData, currCourseData);
        return true;
    }

    public void run() {
        while (true) {
            CourseData prevCourseData = getPreviousCourseData();
            System.out.println("Begin site analysis");
            CourseData currCourseData = getCurrentCourseData();
            if (null == currCourseData) {
                System.err.println("Unable to parse course data");
                continue;
            }
            //CourseData.Crn tmp = new CourseData.Crn("     91112  201 07     CALCULUS C                        4.0   45    4       41    1 MWF     02:41PM 03:36PM SST   122        Jackson Tobin                                                             ");
            //prevCourseData.courseDetails.put("91112-0", tmp);
            //tmp = new CourseData.Crn("     90907  125 24     GENERAL CHEMISTRY LAB I           1.0   12    4        8    0 R       01:00PM 04:00PM SST   252        Snow Mary                                                                 ");
            //prevCourseData.courseDetails.put("90907-0", tmp);
            if ((null != prevCourseData) && (prevCourseData.getTerm().compareTo(currCourseData.getTerm()) == 0)) {
                if (detectChanges(prevCourseData, currCourseData)) {
                    System.out.println("Updating course data.");
                    serializeCourseData(currCourseData);
                }
            } else {
                StringBuilder body = new StringBuilder();
                String subject = "Course Storage Initialized (" + currCourseData.getTerm() + ")";
                for (CourseData.Crn crn : currCourseData.courseDetails.values()) {
                    body.append(crn.getRaw() + "\n");
                }
                System.out.println(body.toString());
                notifier.sendEmail(subject, body.toString(), config.getEmailAddresses());
                System.out.println("Saving initial data.");
                serializeCourseData(currCourseData);
            }

            if (config.getIntervalMinutes() == 0) break;
            try {
                Thread.sleep(config.getIntervalMinutes() * 60 * 1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
