import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import java.io.*;
import java.util.*;


/* ############# Terminal Commands for Automation #################
 * Notes: Use ; on windows to separate jar arguments
 * 		  -cp overrides existing classpath (preferred)
 * 
 * Compile: need to specify class path for external libraries
 * In this case, need javax.mail.jar, jaf-1_1_1.zip. Both are in folder lib. 
 * javac -cp "../lib/*" Email.java
 * 
 * Run: have to include current directory 
 * where class file (Email.class) is, because we are overriding class path with -cp 
 * java -cp ""../lib/*;." Email
 */
public class SendEmailAuto {
	//Random seed for customizing messages to send 
	static Random seed = new Random((int)(Math.random()*248));
	static HashSet<String> randMessages = new HashSet<String>();
	static HashMap<String, String> messages = new HashMap<String, String>();
	//Put all attachments here
	static HashSet<File> attachments = new HashSet<File>();
	
	/* @param name: filename to search for.
	 * @param file: home dir or file to start searching from
	 */
	public static void addMessages(String name){
		String casual = "Hey " + name + ",<br/>(Message here)<br/><br/>Thanks, <br/>William";
		String formal = casual.replace("Hey ", "Hello ");
		String casualNick = casual.replace("William", "Bill");
		String formalNick = formal.replace("William", "Bill");
		messages.put("casual", casual);
		messages.put("formal", casual);
		messages.put("casualNick", casualNick);
		messages.put("formalNick", formalNick);
		messages.put("none", "");
	}
	public static String getRandMessage(){
		String[] rand = randMessages.toArray(new String[randMessages.size()]);
		int next = seed.nextInt(rand.length);
		return rand[next];
	}
	
	//constructs messsage based on commandline arguments
	public static String formulateMessage(String type){
		String toSend;
		if(type.equals("random")){
			toSend = getRandMessage();
		}
		else if(messages.containsKey(type)){
			toSend = messages.get(type);
		}
		else{
			toSend = type;
		}
		return toSend;
	}
	public static File findFile(String name,File file){
		//Queue is interface
		Queue<File> q = new LinkedList<File>();
		for(File c : file.listFiles()){
			if(c.isDirectory() && !c.toString().contains("."))
				q.add(c);
			//check first layer
			else if(c.getName().equals(name))
				return c;
		}
		while(!q.isEmpty()){
			File curr = q.poll();
			//check if the array of files is null
			if(curr.listFiles() != null){
				for(File f : curr.listFiles()){
					if(f.isDirectory())
						q.add(f);
					else if(f.getName().equalsIgnoreCase(name)){
						return f;
					}	
				}
			}
		}
		//if nothing is found
		return null;
    }
	/*@param home: home directory or directory to search in 
	 * 
	 */
	public static void convertToFiles(File home, String[] args){
		if(args.length > 6){
			for(int i = 6; i < args.length; i++){
				File f = findFile(args[i], home);
				attachments.add(f);
			}
			for(File a: attachments){
				if(a.exists()){
					System.out.println(a.getName() + " exists and is being attached.");
				}
			}
		}
	}
	/* @param multi: the Multipart that holds everything together
	 * 
	 */
	public static void addAttachments(Multipart multi){
		if(attachments.size() == 0)
			return;
		
		for(File f: attachments){
			try{
				BodyPart att = new MimeBodyPart();
				//need a data source for the file's path
				FileDataSource attach = new FileDataSource(f.getPath()); 
				att.setDataHandler(new DataHandler(attach));
				att.setFileName(f.getName());
				multi.addBodyPart(att);
			}
			catch(Exception e){
				System.out.println("Error attaching files.");
			}
		}
	}
	
	//To display data
	public static String displayMessage(String message){
		String disp = message.replace("<br/>", "\n");
		return disp;
	}
	public static int countLines(String message){
		String[] s = message.split("\n");
		return s.length;
	}
	public static int countWords(String message){
		String str = message.replace("\n", " ");
		String[] s = str.split("\\s+"); 
		return s.length;
	}
	public static void printAttachments(FileWriter writer) throws IOException{
		if(attachments.size() == 0)
			writer.write("none\n");
		for(File f: attachments){
			writer.write(f.getName()+" ("+f+")\n");
		}
	}
	/* args[0] = from
	 * args[1] = PASSWORD
	 * args[2] = to, separated by commas with no space
	 * args[3] = cc, separated by commas with no space
	 * args[4] = subj
	 * args[5] = message type (random, casual, formal, casualNick, formalNick),(person's name) 
	 * args[6]...args[args.length] = attachments
	 */
	public static void main(String[] args) throws IOException {
		String name = args[5].substring(args[5].indexOf(" ")).trim();
		String type = args[5].substring(0,  args[5].indexOf(" "));
		addMessages(name);
		
		final String USERNAME = args[0];
		final String PASS = args[1];
		final String DEST = args[2];
		final String CC = args[3];
		final String SUBJECT = args[4];
		String MESSAGE = formulateMessage(type);
		//System.exit(0);
		if(!USERNAME.contains("@") || !DEST.contains("@")){
			System.out.println("Invalid username or destination.");
			System.exit(0);
		}
		Scanner scan = new Scanner(System.in);
		System.out.print("Enter password for " + USERNAME+": ");
		String password = scan.next();
		//Home directory 
		scan.close();
		long start = System.currentTimeMillis();

		//put in new properties, set up gmail host, port, authentication
		//basically empty hashmap
		Properties p = new Properties();
		//put it all necessary properties
		p.put("mail.smtp.auth", true);
		p.put("mail.smtp.starttls.enable", true);
		//smtp server address
		p.put("mail.smtp.host", "smtp.gmail.com"); 
		p.put("mail.smtp.port", "587");
		
		//Authenticator object for password
		Authenticator authenticator = new Authenticator(){
			protected PasswordAuthentication getPasswordAuthentication(){
				PasswordAuthentication check = new PasswordAuthentication(USERNAME, password);
				return check;
			}
		};	
		//create new session to send message, with authentication
		//getInstance gets a new Session object, with relevant properties and `authentication
		Session sess = Session.getInstance(p, authenticator);
		
		try{
			//message to send. (add everything to this with setContent(Multipart)
			MimeMessage m = new MimeMessage(sess);
			
			//Check for authentication, else throw AuthenticationException
			Transport trans = sess.getTransport("smtp");
			trans.connect("smtp.gmail.com", USERNAME,  password);
			
			//If authentication succeeds, start attaching files to email
			File home = new File(System.getProperty("user.home"));
			//gets filename from path
			convertToFiles(home, args);
			
			//For multiple senders? Idk how this is even possible
			InternetAddress from = new InternetAddress(USERNAME);
			from.setPersonal("Hi it's me Bill!");
			m.setFrom(from);
			m.setSubject(SUBJECT);
			
			//Array of people I want to CC
			if(!CC.equals("")){
				InternetAddress[] cc = InternetAddress.parse(CC);
				m.addRecipients(Message.RecipientType.CC, cc);
			}
			//Array of people I want to send email to
			InternetAddress[] to = InternetAddress.parse(DEST);
			m.setRecipients(Message.RecipientType.TO, to);

			//for text use multipart to hold everything together
			MimeMultipart multi = new MimeMultipart();
			//Add the message
			MimeBodyPart text= new MimeBodyPart();
			//use hashset for this later
			text.setContent(MESSAGE, "text/html");
			multi.addBodyPart(text);
			
			
			//for attachment, create a new body parts to attach attachments
			addAttachments(multi);
			
			//Sets content of message to include all parts of the multipart
			m.setContent(multi);
			//Actually sends message- send is static method, so use class to access
			
			Transport.send(m);
			long end = System.currentTimeMillis();
			System.out.println("Email sent successfully.");
			
			//Print to log file
			String fname = System.getProperty("user.home")+"\\email_history.txt";
			//True means that it appends to the file, instead of overwriting
			FileWriter f = new FileWriter(fname, true);

			String disp = displayMessage(MESSAGE);
			f.write("SUMMARY: "+"\n");
			f.write("DATE SENT: " + new Date()+
			"\n=======================================\n");
			f.write("FROM: " + USERNAME+"\n");
			f.write("TO: " + DEST+"\n");
			f.write("CC: " + CC+"\n");
			f.write("SUBJECT: " + m.getSubject()+"\n");
			f.write("=======================================\n");
			f.write("MESSAGE (html): \n" +disp + "\n");
			f.write("=======================================\n");
			f.write("MESSAGE-ID: " + m.getMessageID()+"\n");
			f.write("LINES: " + countLines(disp)+"\n");
			f.write("WORDS: " +countWords(disp)+"\n");
			f.write("CHARACTERS: " + disp.length()+"\n");
			f.write("=======================================\n");
			f.write("ATTACHMENTS: \n");
			printAttachments(f);
			f.write("=======================================" );
			f.write("\nEmail took " + (end - start)/1000.0 + " seconds to send."+"\n\n");
			f.write("#########################################################################\n\n");
			f.write("\n");
			f.close();
		}
		catch(MessagingException e){
			System.out.println("Email failed to send. Check username and password.");
		}
	}
}