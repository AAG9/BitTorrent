/**
 * Created by ameya on 11/18/15.
 */
import javax.annotation.processing.FilerException;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Server {
	static int sPort = 9000;    //The server will be listening on this port number
	static String fileName;
	static long fileLength;
	static int totalChunkCount;
	static int chunkCount=0;
	static int extraChunks=0;
	static int finalChunks=0;


	public static void main(String[] args) throws Exception {

		String dir = System.getProperty("user.dir").toString()+"\\Server";

		File sendChunk = new File(dir);

		for(File file: sendChunk.listFiles()) file.delete();

		//Method call to split file
		FileSplit();

		chunkCount=totalChunkCount / 5;
		extraChunks=totalChunkCount % 5;

		ServerSocket listener = new ServerSocket(sPort);
		System.out.println("\nThe server is running.");

		int clientNum = 1;
		try {
			while(true) {
				new Handler(listener.accept(),clientNum).start();
				System.out.println("\nClient "  + clientNum + " is connected!");
				clientNum++;
			}
		} finally {
			listener.close();
		}

	}

	@SuppressWarnings("resource")
	public static void FileSplit() throws IOException {
		//        /home/ameya/Desktop/Canon.mp3
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter a file name: ");
		System.out.flush();
		String filename = scanner.nextLine();
		FileSplit fileSplit = new FileSplit();
//		fileName = "C:/Users/AMEYA/workspace/CNProject_AmeyaChikodi/Canon.mp3";
		File file = new File(filename);
		fileSplit.splitFile(file);
		fileName = file.getName().toString();
		System.out.print("File:" +fileName);
		fileLength = file.length();
		System.out.print("\nFile Size:" +fileLength);
		totalChunkCount = fileSplit.counter;
		System.out.print("\nNumber of chunks:" +totalChunkCount);
	}

	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for dealing with a single client's requests.
	 */
	private static class Handler extends Thread {

		private ObjectInputStream in;	//stream read from the socket
		private ObjectOutputStream out;    //stream write to the socket
		private int no;		//The index number of the client

		Socket connection = null; //socket for the connection with the client
		String MESSAGE;    

		FileInputStream fis;
		BufferedInputStream bis;
		OutputStream os;

		public Handler(Socket connection, int no) {
			this.connection = connection;
			this.no = no;
			run();
		}

		public void run() {
			try{
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());

//				System.out.println("\n"+extraChunks);
				if(extraChunks >= no) finalChunks = chunkCount + 1;
				else finalChunks = chunkCount;
				MESSAGE = fileName + "\t" + fileLength + "\t" + totalChunkCount + "\t" + no;
				System.out.print("\nMessage to client:" + this.MESSAGE);
				String metaData = fileName + "#" + fileLength + "#" + totalChunkCount + "#" + no + "#" + finalChunks;
				sendMessage(metaData);


				String dir = System.getProperty("user.dir").toString()+"\\Server";

				File sendChunk = new File(dir);

				File chunkList[] = sendChunk.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.matches("(.*)chunk(.*)");
					}
				});

				Arrays.sort(chunkList);

				//                for(int i=0;i<chunkList.length;i++){
				//                    System.out.println(chunkList[i]);
				//                }


				if(no==1) {
					for (int i = 0; i < chunkList.length; i = i + 5) {
						if (chunkList[i].canRead()) {
							File file = new File(chunkList[i].toString());
							long fileSize=file.length();
							sendMessageLong(fileSize);
							System.out.println("\nSending Chunk: " + chunkList[i].toString()+" to client"+no);
							this.sendFiles(chunkList[i]);
							//System.out.println("Chunk Sent");
						}
					}
				}
				else if(no==2){
					for (int i = 1; i < chunkList.length; i = i + 5) {
						if (chunkList[i].canRead()) {
							File file = new File(chunkList[i].toString());
							long fileSize=file.length();
							sendMessageLong(fileSize);
							System.out.println("\nSending Chunk: " + chunkList[i].toString()+" to client"+no);
							this.sendFiles(chunkList[i]);
							//System.out.println("Chunk Sent");
						}
					}
				}
				else if(no==3){
					for (int i = 2; i < chunkList.length; i = i + 5) {
						if (chunkList[i].canRead()) {
							File file = new File(chunkList[i].toString());
							long fileSize=file.length();
							sendMessageLong(fileSize);
							System.out.println("\nSending Chunk: " + chunkList[i].toString()+" to client"+no);
							this.sendFiles(chunkList[i]);
							//System.out.println("Chunk Sent");
						}
					}
				}
				else if(no==4){
					for (int i = 3; i < chunkList.length; i = i + 5) {
						if (chunkList[i].canRead()) {
							File file = new File(chunkList[i].toString());
							long fileSize=file.length();
							sendMessageLong(fileSize);
							System.out.println("\nSending Chunk: " + chunkList[i].toString()+" to client"+no);
							this.sendFiles(chunkList[i]);
							//System.out.println("Chunk Sent");
						}
					}
				}
				else if(no==5){
					for (int i = 4; i < chunkList.length; i = i + 5) {
						if (chunkList[i].canRead()) {
							File file = new File(chunkList[i].toString());
							long fileSize=file.length();
							sendMessageLong(fileSize);
							System.out.println("\nSending Chunk: " + chunkList[i].toString()+" to client"+no);
							this.sendFiles(chunkList[i]);
							//System.out.println("Chunk Sent");
						}
					}
				}
				else{
					System.out.println("Maximum peer client limit reached");
				}
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
			finally{
				//Close connections
				try{
					in.close();
					out.close();
					connection.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + no);
				}
			}
		}

		//send a message to the output stream
		public void sendMessage(String msg)
		{
			try{
				out.writeObject(msg);
				out.flush();
				//System.out.println("Send message: " + msg + " to Client " + no);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
		//send a message to the output stream
				public void sendMessageLong(long msg)
				{
					try{
						out.writeObject(msg);
						out.flush();
						//System.out.println("Send message: " + msg + " to Client " + no);
					}
					catch(IOException ioException){
						ioException.printStackTrace();
					}
				}

		void sendFiles(File myFile)
		{
			try {
				byte[] arrby = new byte[(int)myFile.length()];
				this.fis = new FileInputStream(myFile);
				this.bis = new BufferedInputStream(this.fis);
				this.bis.read(arrby, 0, arrby.length);
				this.os = this.connection.getOutputStream();
				System.out.println("Sending File:");
				this.os.write(arrby, 0, arrby.length);
				this.os.flush();
				System.out.println("File Sent");
			}
			catch (IOException var2_3) {
				var2_3.printStackTrace();
			}

		}

	}

}