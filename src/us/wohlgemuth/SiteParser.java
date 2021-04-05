package us.wohlgemuth;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SiteParser {

    public class SiteException extends Exception {
        public SiteException(String msg) {
            super(msg);
        }
    }

    private String term;
    private ArrayList<Configuration.Crn> crns;

    private final String UserAgentString = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

    public SiteParser(String term, ArrayList<Configuration.Crn> crns) {
        this.term = term;
        this.crns = crns;
    }

    private Connection connect(URL url, Connection.Method method) {
        return Jsoup.connect(url.toExternalForm())
                .userAgent(UserAgentString)
                .ignoreHttpErrors(true)
                .method(method);
    }

    private Connection.Response getResponse(Connection connection) throws SiteException {
        Connection.Response response;
        try {
            response = connection.execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SiteException("unable to retrieve page");
        }
        return response;
    }

    private Document getDocument(Connection.Response response) throws SiteException {
        Document doc;
        try {
            doc = response.parse();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SiteException("unable to parse response");
        }
        return doc;
    }

    private Document navigate(URL url, String term, String segment) throws SiteException {
        Connection connection = connect(url, Connection.Method.GET);
        HashMap<String, String> params = new HashMap<>();
        params.put("file", term + ".html");
        params.put("segment", segment);
        connection.data(params);
        Connection.Response response = getResponse(connection);
        Document document = getDocument(response);
        return document;
    }

    private ArrayList<CourseData.Crn> getDetails(Document document, String crn) {
        ArrayList<CourseData.Crn> details = new ArrayList<>();
        String body = document.body().toString();
        body = body.replace("&amp;", "&");
        String[] lines = body.split(System.getProperty("line.separator"));
        for (String line : lines) {
            if ((line.length() >= 10) && (line.substring(5, 10).compareTo(crn) == 0)) {
                CourseData.Crn detail = new CourseData.Crn(line);
                details.add(detail);
            }
        }
        return details;
    }

    private HashMap<String, CourseData.Crn> getCourseDetails() throws SiteException {
        HashMap<String, CourseData.Crn> courseDetails = new HashMap<>();
        Iterator<Configuration.Crn> it = crns.iterator();
        while (it.hasNext()) {
            Configuration.Crn crn = it.next();
            URL url = null;
            String strUrl = "https://www.uah.edu/cgi-bin/schedule.pl";
            try {
                url = new URL(strUrl);
            } catch (MalformedURLException e) {
                throw new SiteException("invalid url: " + strUrl);
            }
            Document document = navigate(url, term, crn.getSegment());
            ArrayList<CourseData.Crn> details = getDetails(document, crn.getId());
            for (int i = 0; i < details.size(); i++) {
                courseDetails.put(crn.getId() + "-" + i, details.get(i));
            }
        }
        return courseDetails;
    }

    public CourseData getCourseData() {
        CourseData courses = null;
        try {
            HashMap<String, CourseData.Crn> courseDetails = getCourseDetails();
            courses = new CourseData(term, courseDetails);
        } catch (SiteException e) {
            e.printStackTrace();
        }
        return courses;
    }

}
