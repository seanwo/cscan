package us.wohlgemuth;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Configuration {

    final private String filename = System.getProperty("user.home") + "/.cscan/config.xml";

    public class ConfigurationException extends Exception {
        public ConfigurationException(String msg) {
            super(msg);
        }
    }

    public class Crn {
        private String id;
        private String segment;

        public Crn(String id, String segment) {
            this.id = id;
            this.segment = segment;
        }

        public String getId() {
            return id;
        }

        public String getSegment() {
            return segment;
        }
    }

    private String smtpHost;
    private String smtpUser;
    private String smtpPassword;
    private Integer intervalMinutes = 0;
    private ArrayList<String> emailAddresses = new ArrayList<>();
    private String term;
    private ArrayList<Crn> crns = new ArrayList<>();

    public Configuration() {
        if (!isFirstTimeConfiguration()) {
            if (!loadConfigurationFile()) {
                throw new RuntimeException("could not load configuration file!");
            }
        }
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public Integer getIntervalMinutes() {
        return intervalMinutes;
    }

    public ArrayList<String> getEmailAddresses() {
        return emailAddresses;
    }

    public String getTerm() {
        return term;
    }

    public ArrayList<Crn> getCrns() {
        return crns;
    }

    private boolean isFirstTimeConfiguration() {
        try {
            File file = new File(filename);
            if (file.exists()) return false;
            File dirs = new File(file.getParent());
            dirs.mkdirs();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElement("configuration");
            doc.appendChild(root);
            Element interval = doc.createElement("interval");
            interval.setAttribute("minutes", "0");
            root.appendChild(interval);
            Element smtp = doc.createElement("smtp");
            smtp.setAttribute("host", "smtp.gmail.com");
            smtp.setAttribute("user", "user@gmail.com");
            smtp.setAttribute("password", "password");
            root.appendChild(smtp);
            Element email = doc.createElement("email");
            email.setAttribute("address", "nobody@domain.com");
            root.appendChild(email);
            Element term = doc.createElement("term");
            term.setAttribute("term", "fall2020");
            root.appendChild(term);
            Element crn = doc.createElement("crn");
            crn.setAttribute("id", "91106");
            crn.setAttribute("segment", "MA");
            root.appendChild(crn);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            Source src = new DOMSource(doc);
            Result dest = new StreamResult(file);
            transformer.transform(src, dest);
        } catch (ParserConfigurationException | TransformerException e) {
            System.err.println("unable to create initial configuration file [" + filename + "]");
            return false;
        }
        System.out.println("created initial configuration file [" + filename + "]");
        System.exit(0);  //exit program on creation of new configuration file
        return true;
    }

    private void loadIntervalNode(Document doc) throws ConfigurationException {
        NodeList nlInterval = doc.getElementsByTagName("interval");
        if (nlInterval.getLength() != 1) {
            throw new ConfigurationException("configuration file must have one interval node");
        }
        Element eInterval = (Element) nlInterval.item(0);
        String minutes = eInterval.getAttribute("minutes");
        if ((null == minutes) || minutes.isEmpty()) {
            throw new ConfigurationException("interval node missing required minutes attribute");
        }
        intervalMinutes = new Integer(minutes);
    }

    private void loadSmtpNode(Document doc) throws ConfigurationException {
        NodeList nlSmtp = doc.getElementsByTagName("smtp");
        if (nlSmtp.getLength() != 1) {
            throw new ConfigurationException("configuration file must have one smtp node");
        }
        Element eSmtp = (Element) nlSmtp.item(0);
        smtpHost = eSmtp.getAttribute("host");
        smtpUser = eSmtp.getAttribute("user");
        smtpPassword = eSmtp.getAttribute("password");
        if ((null == smtpHost) || (smtpHost.isEmpty())) {
            throw new ConfigurationException("smtp node missing required host attribute");
        }
        if ((null == smtpUser) || (smtpUser.isEmpty())) {
            throw new ConfigurationException("smtp node missing required user attribute");
        }
        if ((null == smtpPassword) || (smtpPassword.isEmpty())) {
            throw new ConfigurationException("smtp node missing required password attribute");
        }
    }

    private NodeList loadEmailList(Document doc) throws ConfigurationException {
        NodeList nlEmails = doc.getElementsByTagName("email");
        if (nlEmails.getLength() < 1) {
            throw new ConfigurationException("configuration file must have at least one email node");
        }
        return nlEmails;
    }

    private void loadEmail(Node nEmail) throws ConfigurationException {
        Element eEmail = (Element) nEmail;
        String address = eEmail.getAttribute("address");
        if ((null == address) || address.isEmpty()) {
            throw new ConfigurationException("crn node missing required id attribute");
        }
        emailAddresses.add(address);
    }

    private void loadTermNode(Document doc) throws ConfigurationException {
        NodeList nlTerm = doc.getElementsByTagName("term");
        if (nlTerm.getLength() != 1) {
            throw new ConfigurationException("configuration file must have one term node");
        }
        Element eTerm = (Element) nlTerm.item(0);
        term = eTerm.getAttribute("term");
        if ((null == term) || (term.isEmpty())) {
            throw new ConfigurationException("term node missing required host attribute");
        }
    }

    private NodeList loadCrnList(Document doc) throws ConfigurationException {
        NodeList nlCrns = doc.getElementsByTagName("crn");
        if (nlCrns.getLength() < 1) {
            throw new ConfigurationException("configuration file must have at least one crn node");
        }
        return nlCrns;
    }

    private void loadCrn(Node nCrn) throws ConfigurationException {
        Element eCrn = (Element) nCrn;
        String id = eCrn.getAttribute("id");
        if ((null == id) || id.isEmpty()) {
            throw new ConfigurationException("crn node missing required id attribute");
        }
        String segment = eCrn.getAttribute("segment");
        if ((null == segment) || segment.isEmpty()) {
            throw new ConfigurationException("crn node missing required segment attribute");
        }
        Crn crn = new Crn(id, segment);
        crns.add(crn);
    }

    private boolean loadConfigurationFile() {
        try {
            File file = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            loadIntervalNode(doc);
            loadSmtpNode(doc);
            NodeList nlCrns = loadCrnList(doc);
            for (int i = 0; i < nlCrns.getLength(); i++) {
                Node nCrn = nlCrns.item(i);
                loadCrn(nCrn);
            }
            if (crns.size() < 1) return false;
            loadTermNode(doc);
            NodeList nlEmails = loadEmailList(doc);
            for (int i = 0; i < nlEmails.getLength(); i++) {
                Node nEmail = nlEmails.item(i);
                loadEmail(nEmail);
            }
            if (emailAddresses.size() < 1) return false;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return false;
        } catch (ConfigurationException e) {
            System.err.println(e.getMessage());
            return false;
        }
        return true;
    }
}
