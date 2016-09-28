import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MessageGenerator {
	private Random seed = new Random((int)(Math.random()*248));
	private HashSet<String> randMessages;
	private HashMap<String, String> messages;
	//Put all attachments here
	
	public MessageGenerator(String name){
		randMessages = new HashSet<>();
		messages = new HashMap<>();
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
	public String getSpecificMessage(String type){
		return messages.get(type);
	}
	/* @param name: filename to search for.
	 * @param file: home dir or file to start searching from
	 */
	
	public String getRandMessage(){
		String[] rand = randMessages.toArray(new String[randMessages.size()]);
		int next = seed.nextInt(rand.length);
		return rand[next];
	}
	
	//constructs messsage based on commandline arguments
	public String formulateMessage(String type){
		String toSend = null;
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
	//To display data
	public String displayMessage(String message){
		return message.replace("<br/>", "\n");
	}
	public int countLines(String message){
		String[] s = message.split("\n");
		return s.length;
	}
	public int countWords(String message){
		String str = message.replace("\n", " ");
		String[] s = str.split("\\s+"); 
		return s.length;
	}
}
