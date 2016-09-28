import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.io.File;
/*This program gets all files from inbox of specified gmail account, displays them, and downloads attachments
    Also, option of replying to emails as well.
 */

/*
    TODO: test cc reply, reply attachments, reply all recipients, mark as read, mark as unread, move folders
    TODO: Mark messages as read/unread
 */
public class CheckEmailMain {
    private static HashMap<String, Message> messageMap = new HashMap<>();
    //smtp server name
    private static final String SMTP_ADDRESS = "smtp.gmail.com";
    //imap server name
    private static final String IMAP = "imap.gmail.com";
    private static final String USERNAME = "billx0477@gmail.com";
    //home directory
    private static final File HOME_DIR = new File(System.getProperty("user.home"));

    public static Message[] retrieveUnreadMessages(Store store, String f) throws MessagingException {
        //List All available Folders
/*        Folder[] folder = store.getDefaultFolder().list("*");
        System.out.println("Available Folders: ");
        for (int i = 0; i < folder.length; i++) {
            System.out.println(folder[i]);
        }*/

        Folder check = store.getFolder(f);
        if (!check.exists())
            return null;

        check.open(Folder.READ_WRITE);

        //Use Flags to check for unread messages.
        Flags seen = new Flags(Flags.Flag.SEEN);
        //I want to match all terms that do not match "seen"
        FlagTerm unseen = new FlagTerm(seen, false);
        Message[] m = check.search(unseen);
        for (int i = 0; i < m.length; i++) {
            messageMap.put(m[i].getSubject(), m[i]);
        }
        return m;
    }
    //Gets and downloads attachments for email with specified subject
    public static void parseAttachments(String subject) {
        if(!messageMap.containsKey(subject)){
            return;
        }
        try {
                Message m = messageMap.get(subject);
                //get content of message
                Object content = m.getContent();
                if(content instanceof Multipart){
                    Multipart multi = (Multipart) content;
                    //do something
                    CheckEmailMain.processMultiAttach(m, multi, true);
            }

        }
        catch(Exception e){
            System.out.println(e);
            System.out.println("Error in parsing files.");
        }
    }

    /* Helper method for parseAttachments
       Processes text, attachments for a given message, if the content of the message is multipart
     * Content type?
     * @param message: message to be analyzed for attachments
     * @param multi: multipart of the message
     * @param toDownload: whether or not to download
     */
    public static List<String> processMultiAttach(Message message, Multipart multi, boolean toDownload) throws MessagingException{
        //Traverse bodyparts in multipart
        List<String> attaches = new ArrayList<>();
        for (int i = 0; i < multi.getCount(); i++) {
            BodyPart part = multi.getBodyPart(i);
            String disp = part.getDisposition();
            //If email has attachments.
            if(disp != null && disp.equalsIgnoreCase(Part.ATTACHMENT)) {
                //add attachment names
                attaches.add(part.getFileName());
                if (toDownload){
                    try {
                        System.out.println("Downloading: " + part.getFileName());
                        //prepare to read file input
                        InputStream input = part.getInputStream();
                        //create the new file to write to
                        String down = System.getProperty("user.home");
                        File download = new File(down + "\\Downloads\\" + part.getFileName());
                        if (!download.exists())
                            download.createNewFile();

                        //prepare output stream to write to file
                        FileOutputStream writer = new FileOutputStream(download);

                        //read 50 mb of data
                        byte[] reader = new byte[50000000];
                        //if numBytesRead == -1, then EOF
                        int numBytesRead = 0;
                        while (numBytesRead != -1) {
                            //inputreader reads byte array
                            numBytesRead = input.read(reader);
                            writer.write(reader);
                        }
                        input.close();
                        writer.close();
                    } catch (IOException e) {
                        System.out.println("Error in reading attachments");
                    }
                }
            }
        }
        return attaches;
    }

    //Reply to specific message
    public static void replyToUnreadMessages(String subject, Properties p){
        System.out.println(subject.length());
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter password: ");
        String password = scan.nextLine();

        try {
            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, password);
                }
            };
            //create session object to authenticate and connect
            Session session = Session.getInstance(p, auth);
            //Try to connect
            Transport trans = session.getTransport("smtp");
            trans.connect(SMTP_ADDRESS, USERNAME, password);

            //Get message by subject
            Message toReply;
                if(messageMap.containsKey(subject))
                    toReply = messageMap.get(subject);
                else{
                    System.out.println("No such email.");
                    return;
                }

                    Message replyBody = toReply.reply(false);

                    //This is the person that sent you the email
                    System.out.println("From: " + InternetAddress.toString(toReply.getFrom()));
                    System.out.println("Subject: " + toReply.getSubject());

                    System.out.print("Reply? [y/n] ");
                    String rep = scan.nextLine();
                if (rep.equalsIgnoreCase("y")) {

                    replyBody.setReplyTo(toReply.getFrom());
                    //tell Message object who's sending
                    String from = USERNAME;
                    replyBody.setFrom(new InternetAddress(from));
                    System.out.print("Enter CC (separate by commas): ");
                    String c = scan.nextLine();
                    System.out.println(c.length());
                    if (c.length() != 0 && c.contains("@")) {
                        InternetAddress[] cc = InternetAddress.parse(c);
                        replyBody.addRecipients(Message.RecipientType.CC, cc);
                    }

                    //Construct message
                    MimeMultipart multi = new MimeMultipart();
                    MimeBodyPart text = new MimeBodyPart();
                    System.out.println("Enter message: ");
                    String t = scan.nextLine();
                    text.setContent(t, "text/html");
                    multi.addBodyPart(text);
                    //TODO: add attachments methods
                    System.out.print("Enter attachments (separated by white space): ");
                    String a = scan.nextLine();
                    System.out.println(a);
                    String[] toAttach = a.split(" ");
                    for (int j = 0; j < toAttach.length; j++) {
                        System.out.println(toAttach[j]);
                    }
                    FileAttacher attacher = new FileAttacher();
                    attacher.convertToFiles(HOME_DIR, toAttach);
                    attacher.addAttachments(multi);


                    replyBody.setContent(multi);
                    //Need to specify username, pasword for some reason?
                    Transport.send(replyBody, USERNAME, password);
                    System.out.println("Replied to " + InternetAddress.toString(toReply.getFrom()) + " successfully.");
                }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    //TODO: Display actual message from sender.
    public static void printInfo(Message[] messages) throws MessagingException, IOException{
        System.out.println("You have " + messages.length +" new unread messages.");
        System.out.println("------------------------------------------------------\n");
        for(Message a: messages){

            System.out.println("Date sent: " + a.getReceivedDate());
            System.out.println("Subject: "+ a.getSubject());
            System.out.println("From: " + InternetAddress.toString(a.getFrom()));
            //The one that you actually wanted to reply to, in case of forwarded messsages
            System.out.println("Reply-To: " + InternetAddress.toString(a.getReplyTo()));
            //The original receiver of the message
            System.out.println("To: " + InternetAddress.toString(a.getRecipients(Message.RecipientType.TO)));

            Object content = a.getContent();
            if(content instanceof Multipart) {
                Multipart multi = (Multipart) content;
                List<String> att=processMultiAttach(a, multi, false);
                System.out.println("Attachments (" + att.size() +"): " + att);
            }
            System.out.println("################################################");
            System.out.println("");
        }
    }

    public static String[] getInfo(String str){
        String replaced = "";
        String command = "";
        loop: for (int i = 0; i < str.length(); i++) {
            if(str.substring(i, i+1).equals(" ")){
                replaced = str.substring(i);
                command = str.substring(0,i);
                break loop;
            }
        }
        String[] info = {command,replaced};
        return info;
    }

    public static void main(String[] args){

        Scanner scan = new Scanner(System.in);
        System.out.print("Enter password: ");
        String password = scan.nextLine();
        //Create properties field to store properties
        try {
            Properties props = System.getProperties();
            //Use imaps server to receive
            props.setProperty("mail.store.protocol", "imaps");
            props.put("mail.imap.starttls.enable", true);
            props.put("mail.imap.host", IMAP);
            props.setProperty("mail.imap.port", "993");

            //Use smtp server to send
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable", true);
            //smtp server address
            props.put("mail.smtp.host", SMTP_ADDRESS);
            props.put("mail.smtp.port", "587");


            //create session and connect to current host
            Session session = Session.getDefaultInstance(props, null);

            //store connecting to imap host
            Store store = session.getStore("imaps");
            store.connect(IMAP, USERNAME, password);

            //List All available Folders
            Message[] messages = retrieveUnreadMessages(store, "Inbox");
            CheckEmailMain.printInfo(messages);

            //TODO: Test multiple attachments
            String option = "";

            while(!option.equalsIgnoreCase("exit")){
                System.out.println("Options: Reply, Download attachments");
                System.out.println("To Reply, type \"reply \" and subject line.");
                System.out.println("To Download attachments, type \"download \" and subject line.");
                option = scan.nextLine();

                if(option.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                }

                if(option.contains("reply") || option.contains("download")){
                    String[] info = getInfo(option);
                    if(info[0].equalsIgnoreCase("reply"))
                        replyToUnreadMessages(info[1].trim(), props);
                    else if(info[0].equalsIgnoreCase("download"))
                        parseAttachments(info[1].trim());
                    else
                        System.out.println("Error. Choose either reply or download.");
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
            System.out.println("Error occurred.");
        }
    }
}
