import javax.swing.*;
import javax.mail.*;
import java.awt.*;

public class GUIAuthenticator extends Authenticator {

	private JTextField userText = new JTextField(20);
	private JPasswordField passText = new JPasswordField(20);
	private JTextField to = new JTextField(20);
	private JTextField cc = new JTextField(20);
	private JTextField subject = new JTextField(50);
	private JTextArea message = new JTextArea(10, 20);
	private JTextField attach = new JTextField(30);

	GUIAuthenticator(String username) {
		JPanel auth = new JPanel();
		auth.setLayout(new BoxLayout(auth, BoxLayout.PAGE_AXIS));
		auth.add(new JLabel("Enter Username: "));
		//default username
		userText.setText(username);
		auth.add(userText);
		auth.add(new JLabel("Enter Password: "));
		auth.add(passText);
		JOptionPane.showMessageDialog(null, auth, "Verification", JOptionPane.PLAIN_MESSAGE);

	}

	void showMessage() {
		//change name of "ok" button to "Send"
		UIManager.put("OptionPane.okButtonText", "Send");
		//Put everything in here
		JPanel m = new JPanel();
		//so elements stack on top of each other
		m.setLayout(new BoxLayout(m, BoxLayout.PAGE_AXIS));

		//Add label, textinput, align left
		JPanel t = new JPanel(new FlowLayout(FlowLayout.LEFT));
		t.add(new JLabel("To: "));
		t.add(to);
		m.add(t);

		//add label, textinput, align left
		JPanel c = new JPanel(new FlowLayout(FlowLayout.LEFT));
		c.add(new JLabel("CC: "));
		c.add(cc);
		m.add(c);

		//add Subject
		JPanel s = new JPanel(new FlowLayout(FlowLayout.LEFT));
		s.add(new JLabel("Subject: "));
		s.add(subject);
		m.add(s);
		//add message label
		//JPanel b = new JPanel(new FlowLayout(FlowLayout.LEFT));
		//b.add(new JLabel("Message: "));
		//m.add(b);
		//add scrollable message box
		JScrollPane scroll = new JScrollPane(message);
		m.add(scroll);

		//add attachments
		JPanel att = new JPanel(new FlowLayout(FlowLayout.LEFT));
		att.add(new JLabel("Attachments: (Just type file name)"));
		att.add(attach);
		m.add(att);
		//m.setVisible(true);
		JOptionPane.showMessageDialog(null, m, "Send message", JOptionPane.PLAIN_MESSAGE);
	}
    public void showErrorMessage(){
       JPanel errorMessage = new JPanel();
        errorMessage.add(new JLabel("Error in sending message."));
        //Change button back to ok
        UIManager.put("OptionPane.okButtonText", "OK");
        JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

	public PasswordAuthentication getPasswordAuthentication() {
		String password = getPass();
		String user = userText.getText();
		PasswordAuthentication check = new PasswordAuthentication(user, password);
		passText.setText("");
		return check;
	}



    String getUsername() {
		return userText.getText();
	}
	String getPass(){
		return new String(passText.getPassword());
	}
	String getMessage() {
        String str = message.getText();
        //Because bodypart reads text in html, so preserves newlines
        str = str.replace("\n", "<br/>");
		return str;
	}

	String getTo() {
		return to.getText();
	}

	String getCC() {
		return cc.getText();
	}

	String getSubject() {
		return subject.getText();
	}

	String[] getAttachments() {
		return attach.getText().trim().split(" ");
	}
}