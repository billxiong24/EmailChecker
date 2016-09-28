import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import java.io.*;
import java.util.*;


public class SendEmailGUI {
    private String smtp_address;
    private String username;

    public SendEmailGUI(String user, String smtp){
        username = user;
        smtp_address = smtp;
    }
    public String getUser(){
        return username;
    }
    public String getSmtp(){
        return smtp_address;
    }

    public void sendMessage(String username, String smtp_address, GUIAuthenticator auth, FileAttacher attacher){
        //start time
        long start = System.currentTimeMillis();

        //basically empty hashmap
        Properties p = new Properties();
        //put it all necessary properties
        //put in new properties, set up gmail host, port, authentication
        //p.put("mail.smtp.ssl.trust", "smtp.office365.com");
        p.put("mail.smtp.auth", true);
        p.put("mail.smtp.starttls.enable", true);
        //smtp server address
        p.put("mail.smtp.host", smtp_address);
        p.put("mail.smtp.port", "587");

        //Authenticator object for password
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return auth.getPasswordAuthentication();
            }
        };
        //create new session to send message, with authentication
        //getInstance gets a new Session object, with relevant properties and `authentication
        Session sess = Session.getInstance(p, authenticator);

        try {
            //message to send. (add everything to this with setContent(Multipart)
            MimeMessage m = new MimeMessage(sess);

            //Check for authentication, else throw AuthenticationException
            Transport trans = sess.getTransport("smtp");
            trans.connect(smtp_address, auth.getUsername(), auth.getPass());

            auth.showMessage();
            String getTo = auth.getTo();
            String getCC = auth.getCC();
            String message = auth.getMessage();
            String subj = auth.getSubject();
            String[] attaches = auth.getAttachments();

            //If authentication succeeds, start attaching files to email
            File home = new File(System.getProperty("user.home"));
            //gets filename from path
            attacher.convertToFiles(home, attaches);

            //For multiple senders? Idk how this is even possible
            InternetAddress from = new InternetAddress(username);
            from.setPersonal("Hi it's me Bill!");
            m.setFrom(from);
            m.setSubject(subj);

            //Array of people I want to CC
            if (!auth.getCC().equals("")) {
                InternetAddress[] cc = InternetAddress.parse(getCC);
                m.addRecipients(Message.RecipientType.CC, cc);
            }
            //Array of people I want to send email to
            InternetAddress[] to = InternetAddress.parse(getTo);
            m.setRecipients(Message.RecipientType.TO, to);

            //for text use multipart to hold everything together
            MimeMultipart multi = new MimeMultipart();
            //Add the message
            MimeBodyPart text = new MimeBodyPart();
            //use hashset for this later
            text.setContent(message, "text/html");
            multi.addBodyPart(text);


            //for attachment, create a new body parts to attach attachments
            attacher.addAttachments(multi);

            //Sets content of message to include all parts of the multipart
            m.setContent(multi);
            //Actually sends message- send is static method, so use class to access
            Transport.send(m);
            long end = System.currentTimeMillis();
            System.out.println("Email sent successfully.");

            //Print to log file
            String fname = System.getProperty("user.home") + "\\email_history.txt";
            //True means that it appends to the file, instead of overwriting
            FileWriter f = new FileWriter(fname, true);

            MessageGenerator generator = new MessageGenerator(message);
            String disp = generator.displayMessage(message);
            f.write("SUMMARY: " + "\n");
            f.write("DATE SENT: " + new Date() +
                    "\n=======================================\n");
            f.write("FROM: " + username + "\n");
            f.write("TO: " + getTo + "\n");
            f.write("CC: " + getCC + "\n");
            f.write("SUBJECT: " + m.getSubject() + "\n");
            f.write("=======================================\n");
            f.write("MESSAGE (html): \n" + disp + "\n");
            f.write("=======================================\n");
            f.write("MESSAGE-ID: " + m.getMessageID() + "\n");
            f.write("LINES: " + generator.countLines(disp) + "\n");
            f.write("WORDS: " + generator.countWords(disp) + "\n");
            f.write("CHARACTERS: " + disp.length() + "\n");
            f.write("=======================================\n");
            f.write("ATTACHMENTS: \n");
            attacher.printAttachments(f);
            f.write("=======================================");
            f.write("\nEmail took " + (end - start) / 1000.0 + " seconds to send." + "\n\n");
            f.write("#########################################################################\n\n");
            f.write("\n");
            f.close();
        } catch (Exception e) {
            auth.showErrorMessage();
            System.out.println("Email failed to send. Check username and password.");
        }
    }
}