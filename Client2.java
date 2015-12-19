/**
 * Created by ameya on 11/18/15.
 */
import static java.lang.Thread.sleep;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Client2 {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server

	FileOutputStream fos;
	BufferedOutputStream bos;
	InputStream is;
	byte [] myByteArray;
	int bytesRead;
	int current;
	int chunkCount;
	int FILE_SIZE;
	static int clientNo;
	int ctr;
	static int port=9002;
	public static String fileName;
	public static int totalChunkCount;
	public static int numberChunksOwn=0;
	long msg;
	public void Client() {}

	void run() throws IOException, ClassNotFoundException {
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 9000);
			System.out.println("Connected to localhost in port 9000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());

			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

			//            System.out.print("Hello, please input a sentence: ");
			//            //read a sentence from the standard input
			//            message = bufferedReader.readLine();
			//            //Send the sentence to the server
			//
			//            sendMessage(message);
			//            //Receive the upperCase sentence from the server

			MESSAGE = (String)in.readObject();
			//show the message to the user
			System.out.println("Receive message: " + MESSAGE);

			List<String> metaData = Arrays.asList(MESSAGE.split("\\s*#\\s*"));

			fileName=metaData.get(0);
			
			chunkCount=Integer.parseInt(metaData.get(4));

			FILE_SIZE = Integer.parseInt(metaData.get(1));

			clientNo = Integer.parseInt(metaData.get(3));
			
			totalChunkCount = Integer.parseInt(metaData.get(2));

			ctr=clientNo;

			for (int i = 0; i<= chunkCount-1;i++) {
				long chunkSize=receiveMessageLong();
				myByteArray = new byte[102400];
				is = requestSocket.getInputStream();
				bytesRead = is.read(myByteArray, 0, myByteArray.length);
				current = bytesRead;
				if(chunkSize-65536>0) 
					do{
						bytesRead = is.read(myByteArray, current, myByteArray.length - current);
						if (bytesRead >= 0) current += bytesRead;
					}while (current != chunkSize);

				String dir = System.getProperty("user.dir").toString()+"\\Client"+clientNo+"\\"+fileName+".chunk";
				String sdir=dir.concat(String.format("%02d", ctr));

				fos = new FileOutputStream(sdir);
				bos = new BufferedOutputStream(fos);
				//recFile = new File("/home/ameya/Desktop/recFile.txt");

				bos.write(myByteArray, 0, current);
				bos.flush();

				System.out.println("Received Chunk No: " +ctr+" from Server");
				ctr=ctr+5;
			}

		}
		catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
		}
		catch ( ClassNotFoundException e ) {
			System.err.println("Class not found");
		}
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}



		
		new Thread(new UploadHandler()).start();
		new Thread(new DownloadHandler()).start();
		



	}
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
			System.out.println("Send message: " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	public long receiveMessageLong(){
		try {
			msg = (long)in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return msg;
	}


	//main method
	public static void main(String args[]) throws IOException, ClassNotFoundException {
		Client2 client = new Client2();
		client.run();
	}


	private static class DownloadHandler implements Runnable{

		Socket downloadSocket;           //socket connect to the server
		ObjectOutputStream out;         //stream write to the socket
		ObjectInputStream in;          //stream read from the socket
		String message;                //message send to the server
		String MESSAGE;                //capitalized message read from the server
		String getChunkList;
		FileOutputStream fos;
		BufferedOutputStream bos;
		InputStream is;
		byte [] myByteArray;
		int bytesRead;

		public DownloadHandler() throws IOException, ClassNotFoundException {
			System.out.println("Download handler start");
			
			
		}

		private void runDownload() throws IOException, ClassNotFoundException {
			
			int current;
			System.out.println("connected to localhost in port 9003");
			while (numberChunksOwn<totalChunkCount) {

				//                sendMessage("REQUEST");
				                System.out.println("DOWNLOAD: Request for chunks");
				//                sendChunksOwned(chunksOwned());
				                System.out.println("DOWNLOAD: Send a chunk ID list");

				int j=0;
				int count=0;

				
				sendChunksOwned(chunksOwned());
				do{
					MESSAGE = (String)in.readObject();
					String msg="NONE"+"#"+"NONE"+"#"+"NONE";
					if(MESSAGE.equals(msg)){break;}
					//show the message to the user
					System.out.println("Receive message: " + MESSAGE);

					List<String> metaData = Arrays.asList(MESSAGE.split("\\s*#\\s*"));
					int chunkNo=Integer.parseInt(metaData.get(0));
					int totalChunks=Integer.parseInt(metaData.get(1));
					long chunkSize=Long.parseLong(metaData.get(2));
					if(j==0) count=totalChunks;
					myByteArray = new byte[102400];
					is = downloadSocket.getInputStream();
					bytesRead = is.read(myByteArray, 0, myByteArray.length);
					current = bytesRead;
					if(chunkSize-65536>0) 
						do{
							bytesRead = is.read(myByteArray, current, myByteArray.length - current);
							if (bytesRead >= 0) current += bytesRead;
						}while (current != chunkSize);

					String dir = System.getProperty("user.dir").toString()+"\\Client"+clientNo+"\\"+fileName+".chunk";
					String sdir = dir.concat(String.format("%02d", chunkNo));

					fos = new FileOutputStream(sdir);
					bos = new BufferedOutputStream(fos);

					bos.write(myByteArray, 0, current);
					bos.flush();

					System.out.println("Received Chunk No:" + chunkNo+"from peer");
					count--;
					j=1;
				}while(count>0);
			}
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(numberChunksOwn==totalChunkCount){
				FileSplit.joinFile(fileName, System.getProperty("user.dir").toString()+"/Client"+clientNo);
				System.out.println("************  All chunks received and are being joined. ************");
			}
		}
		String readConfig(int clientNo){
			String[] tokens;
			int i=0;
			String[][] config = new String[10][10];
			try{
				FileInputStream fstream = new FileInputStream(System.getProperty("user.dir").toString()+"/Config.txt");
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;

				while ((strLine = br.readLine()) != null){
					tokens = strLine.split(" ");
					if(!strLine.isEmpty()){
						for(int j=0;j<tokens.length;j++){
							config[i][j]=tokens[j];
						}
						i++;
					}
				}
			}catch (Exception e){
				System.err.println("Error: " + e.getMessage());
			}finally {
				try {
					if(in!=null)in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			switch (clientNo+1){
			case(2):
				return config[2][1];
			case(3):
				return config[3][1];
			case(4):
				return config[4][1];
			case(5):
				return config[5][1];
			case(6):
				return config[1][1];
			default:
				return "error";
			}

		}

		private void sendMessage(String msg)
		{
			try{
				//stream write the message
				out.writeObject(msg);
				out.flush();
				System.out.println("Send message: " + msg);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
		private void sendChunksOwned(List msg)
		{
			try{
				//stream write the message
				out.writeObject(msg);
				out.flush();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}

		private String receiveMessage(){
			try {
				MESSAGE = (String)in.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return MESSAGE;
		}

		private List chunksOwned(){
			System.out.println("In chunks owned by Me");
			List<Integer> chunksOwned;
			chunksOwned=new ArrayList<Integer>();
			String dir = System.getProperty("user.dir").toString()+"\\Client"+clientNo;

			File sendChunk = new File(dir);

			File chunkList[] = sendChunk.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches("(.*)chunk(.*)");
				}
			});

			Arrays.sort(chunkList);
			for(int i=0;i<chunkList.length;i++){
				List<String> list = Arrays.asList(chunkList[i].toString().split(Pattern.quote(".")));
				chunksOwned.add(Integer.parseInt(list.get(2).substring(5)));
			}
			System.out.println(chunksOwned);
			numberChunksOwn = chunksOwned.size();
			return chunksOwned;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			do{
				try {
					String token=readConfig(clientNo);
					//System.out.println(token);
					downloadSocket = new Socket("localhost", Integer.parseInt(token));
					if(downloadSocket==null){
						System.out.println("Cannot connect to download neighbour. Retry after 1 sec.");
						try {
							sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}catch (SocketException e){
					System.err.println("Cannot connect to download neighbour. Retry after 1 sec.");
					try {
						sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}while(downloadSocket==null);
			//initialize inputStream and outputStream
			try {
				out = new ObjectOutputStream(downloadSocket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(downloadSocket.getInputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			try {
				runDownload();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static class UploadHandler implements Runnable{

		Socket uploadSocket;           //socket connect to the server
		ObjectOutputStream out;         //stream write to the socket
		ObjectInputStream in;          //stream read from the socket
		String message;                //message send to the server
		String MESSAGE;                //capitalized message read from the server
		List<Integer> chunksOwnedByDownloader;
		List<Integer> chunksOwnedByMe;
		List<Integer> tempChunksOwnedByDownloader;
		FileInputStream fis;
		BufferedInputStream bis;
		OutputStream os;

		public UploadHandler() throws IOException {
			
			
			
		}

		private void uploadRun() throws IOException{
			while (true) {
				//                String str = receiveMessage();
				//                do {
				//                    if (str == "REQUEST") {
				//                        System.out.println("UPLOAD: Receive request from upload neighbour.");
				//                    }
				//                }while(!str.equals("REQUEST"));

				String chunksOwned = "";

				receiveChunksOwnedList();
				for (int i = 0; i < chunksOwnedByDownloader.size(); i++) {
					chunksOwned = chunksOwned + chunksOwnedByDownloader.get(i).toString() + "\t";
				}
				System.out.println("UPLOAD: upload neighbour has chunk: " + chunksOwned);

				chunksOwnedByMe = chunksOwned();



				tempChunksOwnedByDownloader = chunksOwnedByDownloader;
				for (int i = 0; i < chunksOwnedByMe.size(); i++) {
					if (!tempChunksOwnedByDownloader.contains(chunksOwnedByMe.get(i))) {
						int f = chunksOwnedByMe.get(i);
						

						String dir = System.getProperty("user.dir").toString()+"\\Client"+clientNo;
						File sendChunk = new File(dir);

						File chunkList[] = sendChunk.listFiles(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								return name.endsWith(String.format("%02d", f));
							}
						});

						String totalChunks= String.valueOf(chunksOwnedByMe.size());
						File file = new File(chunkList[0].toString());
						long fileSize=file.length();
						String fs=String.valueOf(fileSize);
						String msg=f+"#"+totalChunks+"#"+fs;
						sendMessage(msg);
						sendFiles(chunkList[0]);
						System.out.println("UPLOAD: upload neighbour has been sent chunk: " + chunksOwnedByMe.get(i));
					}else{
						String msg="NONE"+"#"+"NONE"+"#"+"NONE";
						sendMessage(msg);
					}
				}
			}
		}

		private void sendFiles(File myFile)
		{
			try {
				byte[] arrby = new byte[(int)myFile.length()];
				this.fis = new FileInputStream(myFile);
				this.bis = new BufferedInputStream(this.fis);
				this.bis.read(arrby, 0, arrby.length);
				this.os = this.uploadSocket.getOutputStream();
				// System.out.println("Sending File:");
				this.os.write(arrby, 0, arrby.length);
				this.os.flush();
				//System.out.println("File Sent");
			}
			catch (IOException var2_3) {
				var2_3.printStackTrace();
			}

		}

		private String receiveMessage() {
			try {
				MESSAGE = (String)in.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			//            //show the message to the user
			//            System.out.println("Receive message: " + MESSAGE);
			return MESSAGE;
		}
		private void receiveChunksOwnedList() {
			//            chunksOwnedByDownloader = new ArrayList<Integer>();
			try {
				chunksOwnedByDownloader = (List<Integer>) in.readObject();
				System.out.println(chunksOwnedByDownloader);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		private List chunksOwned(){
			List<Integer> chunksOwned = new ArrayList<Integer>();
			String dir = System.getProperty("user.dir").toString()+"\\Client"+clientNo;

			File sendChunk = new File(dir);

			File chunkList[] = sendChunk.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches("(.*)chunk(.*)");
				}
			});

			Arrays.sort(chunkList);
			for(int i=0;i<chunkList.length;i++){
				List<String> list = Arrays.asList(chunkList[i].toString().split(Pattern.quote(".")));
				chunksOwned.add(Integer.parseInt(list.get(2).substring(5)));
			}
			return chunksOwned;
		}

		private void sendMessage(String msg) {
			try{
				//stream write the message
				sleep(1000);
				out.writeObject(msg);
				out.flush();
				System.out.println("Send message: " + msg);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("The Client"+clientNo+" server is running.");
			ServerSocket listener;
			try {
				listener = new ServerSocket(port);
				uploadSocket=listener.accept();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//initialize inputStream and outputStream
			try {
				out = new ObjectOutputStream(uploadSocket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(uploadSocket.getInputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				uploadRun();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

}