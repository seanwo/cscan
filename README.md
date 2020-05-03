# CScan (UAH CRN Scanner)

This command line application was designed to navigate the [UAH class schedules](https://www.uah.edu/cgi-bin/schedule.pl) website and detect changes for a list of CRNs.  Upon detecting a CRN change for the specified term, the system is designed to send an email to a list of email addresses that shows the changes for that CRN.

To setup the system you will need:

+ Access to a SMTP server (along with username and password) such as [smtp.gmail.com](https://support.google.com/a/answer/176600?hl=en)
+ The SMTP servers username and password used to send outgoing mail (typically your gmail credentials for above server).
+ The term of the CRNs you are monitoring
+ A list of CRNs you are interested in monitoring
+ List of email addresses you want notified when assignments/grades change

*If you are going to use a gmail account to send notifications, I recommend setting up a separate account to do this and not use your personal account.*

Build a cscan.jar file using the included manifest.

Execute using:

```
java -jar cscan.jar
```

When run for the first time it will create a sample config.xml file in a .cscan directory off of your user home directory (~/.scan for linux %USERPROFILE%\.cscan for windows).  Edit this file with the above specifics and set the polling interval.  An interval of 0, executes the code once and terminates.

Upon running it for the first time for any set of CRNs (handles CRNs) it will store the current state of those classes off as a baseline and will not notify you until subsequent changes.



