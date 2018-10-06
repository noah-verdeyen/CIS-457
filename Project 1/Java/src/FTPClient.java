import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import javax.swing.*;

public class FTPClient {

	public static void main(String argv[]) throws Exception {
		String sentence;
		String modifiedSentence = "";
		boolean isOpen = true;
		int number = 1;
		boolean notEnd = true;
		String statusCode;
		boolean clientgo = true;
		int port;

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		sentence = inFromUser.readLine();
		StringTokenizer tokens = new StringTokenizer(sentence);

		if (sentence.startsWith("connect")) {
			String serverName = tokens.nextToken(); // pass the connect command
			serverName = tokens.nextToken();
			int port1 = Integer.parseInt(tokens.nextToken());
			System.out.println("You are connected to " + serverName);

			Socket ControlSocket = new Socket(serverName, port1);
			DataOutputStream outToServer = new DataOutputStream(ControlSocket.getOutputStream());
			DataInputStream inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));

			while (isOpen && clientgo) {
				sentence = inFromUser.readLine();
				modifiedSentence = "";

				if (sentence.equals("list:")) {

					port = port1 + 2;
					outToServer.writeBytes(port + " " + sentence + " " + '\n');
					
					ServerSocket welcomeData = new ServerSocket(port);
					Socket dataSocket = welcomeData.accept();
					DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
					while (inData.available() == 0)
						Thread.sleep(20);

					
					while (inData.available() > 0) 
						modifiedSentence += inData.readUTF();
					
					System.out.println("List is \n" + modifiedSentence);

					welcomeData.close();
					dataSocket.close();
				} else if (sentence.startsWith("retr:")) {
					port = port1 + 2;
					outToServer.writeBytes(port + " " + sentence + " " + '\n');
					ServerSocket welcomeData = new ServerSocket(port);
					Socket dataSocket = welcomeData.accept();
					DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
					
					byte[] dataIn = new byte[inData.readInt()];
					
					//readUTF was giving errors, so just parse the sentence
					String[] temp = sentence.split(" ");
					String filePath = temp[temp.length - 1];
					filePath = filePath.substring(0, filePath.indexOf(".")) 
							+ "_received" + filePath.substring(filePath.indexOf("."));
					inData.readFully(dataIn);
					try (FileOutputStream fos = new FileOutputStream(filePath)) {
						   fos.write(dataIn);
					}
					inData.close();
					welcomeData.close();
					dataSocket.close();
				} else if (sentence.startsWith("stor:")) {
					JFileChooser chooser=new JFileChooser();
					chooser.showSaveDialog(null);
					File fileToSend = chooser.getSelectedFile();
					FileInputStream fis = new FileInputStream(fileToSend);
					BufferedInputStream bis = new BufferedInputStream(fis);
					System.out.println(fileToSend.getAbsolutePath());
					port = port1 + 2;
					outToServer.writeBytes(port + " " + sentence + " " + '\n');
					
					ServerSocket welcomeData = new ServerSocket(port);
					Socket dataSocket = welcomeData.accept();
					DataOutputStream outData = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
					
					long length = fileToSend.length();
					byte[] bytesToSend = new byte[(int) length];
					bis.read(bytesToSend, 0, (int)length);
					outData.writeInt((int)length);
					outData.writeUTF(fileToSend.getAbsolutePath());
					outData.write(bytesToSend);
					bis.close();
					fis.close();
					outData.close();
					
					welcomeData.close();
					dataSocket.close();
					
				} else if (sentence.equals("quit:")) {
					outToServer.writeBytes(sentence + " " + '\n');
					clientgo = false;
				}
				System.out.println("\nWhat would you like to do next: \n retr: file.txt ||stor: file.txt  || close\n\n");
			}
			
			ControlSocket.close();
		}
	}
}