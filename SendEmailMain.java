/**
 * Created by William Xiong on 6/23/2016.
 * GUI for sending emails from specified username and server name (gmail server)
 * FileAttacher
 * GUIAuthenticator
 * MessageGenerator
 * SendEmailGUI
 */
public class SendEmailMain {

    public static void main(String[] args){
        //create object to send email
        SendEmailGUI send = new SendEmailGUI("billx0477@gmail.com", "smtp.gmail.com");

        //username, server name
        String user = send.getUser();
        String smtp = send.getSmtp();

        GUIAuthenticator auth = new GUIAuthenticator(send.getUser());
        FileAttacher attacher = new FileAttacher();
        send.sendMessage(user, smtp, auth, attacher);
    }
}
