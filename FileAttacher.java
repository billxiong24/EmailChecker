import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;

public class FileAttacher {
	 private HashSet<File> attachments;
	
	 public FileAttacher(){
		attachments = new HashSet<>();
	 }
	 
	 
	 public File findFile(String name,File file){
		//Queue is interface
		Queue<File> q = new LinkedList<>();
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
	public void convertToFiles(File home, String[] args){
        //If attachments box is empty
        if(args[0].equals("") || args == null)
            return;
		if(args.length > 0){
			for(int i = 0; i < args.length; i++){
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
	public void addAttachments(Multipart multi){
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
	public void printAttachments(FileWriter writer) throws IOException{
		if(attachments.size() == 0)
			writer.write("none\n");
		for(File f: attachments){
			writer.write(f.getName()+" ("+f+")\n");
		}
	}
}
