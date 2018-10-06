import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class FTPServerThread extends Thread {
	private Socket controlSocket;
	private int currentSocket;
	private DataOutputStream outToClient;
	private BufferedReader inFromClient;
	private List<String> clientTokens;
	private String clientCommand;
	private boolean clientGo;
	private int clientPort;
	private String fromClient;
	
	public FTPServerThread(Socket controlSocket,int currentSocket, int port)
    {
		clientGo = true;
		this.controlSocket = controlSocket;
        this.currentSocket = currentSocket;
        clientTokens = new ArrayList<String>();
		try {
			outToClient = new DataOutputStream(controlSocket.getOutputStream());
			inFromClient = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Error: " + e.toString());
		}
    }
	
	//Might be deprecated? Not sure, it automatically overrides without this,
	//but helps readability to show this overrides the super run() method.
	@Override
	public void run() {
		try {
			while (clientGo) {
				String fromClient = inFromClient.readLine();
				StringTokenizer tokens = null;
				if (!fromClient.equals(null))
					tokens = new StringTokenizer(fromClient);
//				
//				if (clientTokens.size() > 0)
//					clientPort = Integer.parseInt(clientTokens.get(0));
				clientTokens.clear();
//				if (clientPort != 0)
//					clientTokens.add(Integer.toString(clientPort));
				
				while (tokens.hasMoreTokens())
					clientTokens.add(tokens.nextToken());
				
				System.out.println(clientTokens);
				if (clientTokens.size() > 1)
					clientCommand = clientTokens.get(1);
				else 
					clientCommand = clientTokens.get(0);
				
				if (clientCommand.equals("list:")) {
					Socket dataSocket = makeDataSocket(Integer.parseInt(clientTokens.get(0)));
					//frstln = tokens.nextToken();
					DataOutputStream dataOutToClient = new DataOutputStream(dataSocket.getOutputStream());
					String files = getAllFiles();
					dataOutToClient.writeUTF(files);
					dataOutToClient.flush();
					dataOutToClient.close();
					System.out.println("List sent successfully");
					dataSocket.close();
					System.out.println("Data Socket closed.\n");
	
				} else if (clientCommand.equals("retr:")) {
					Socket dataSocket = makeDataSocket(Integer.parseInt(clientTokens.get(0)));
					if (clientTokens.size() < 3) {
						System.out.println("No file supplied.");
						return;
					}
					File fileToSend = new File(clientTokens.get(2));
					if (!fileToSend.exists()) {
						System.out.println("File does not exist.");
						return;
					}
					FileInputStream fis = new FileInputStream(fileToSend);
					BufferedInputStream bis = new BufferedInputStream(fis);
					DataOutputStream outData = new DataOutputStream(new BufferedOutputStream(dataSocket.getOutputStream()));
					
					long length = fileToSend.length();
					byte[] bytesToSend = new byte[(int) length];
					bis.read(bytesToSend, 0, (int)length);
					outData.writeInt((int)length);
					outData.write(bytesToSend);
					bis.close();
					fis.close();
					outData.close();
					
				} else if (clientCommand.equals("stor:")) {
					Socket dataSocket = makeDataSocket(Integer.parseInt(clientTokens.get(0)));
					DataInputStream inData = new DataInputStream(new BufferedInputStream(dataSocket.getInputStream()));
					byte[] dataIn = new byte[inData.readInt()];
					while (inData.available() == 0)
						Thread.sleep(20);
					
					String filePath = inData.readUTF();
					filePath = filePath.substring(0, filePath.indexOf(".")) 
							+ "_stored" + filePath.substring(filePath.indexOf("."));
					inData.readFully(dataIn);
					try (FileOutputStream fos = new FileOutputStream(filePath)) {
						   fos.write(dataIn);
					}
					inData.close();
					dataSocket.close();
				} else if (clientCommand.equals("quit:")) {
					FTPServer.quit();
					outToClient.close();
					inFromClient.close();
					clientGo = false;
				}
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
			e.printStackTrace();
		}
	}
	
	private Socket makeDataSocket(int port) {
		System.out.println("Making data socket...");
		try {
			return new Socket(controlSocket.getInetAddress(), port);
		} catch (IOException e) {
			System.out.println("Error: " + e.toString());
		}
		return null;
	}
	
	private static String getAllFiles() {
		File curDir = new File(".");
		File[] files = curDir.listFiles();
		String retStr = "";
		
		for (int i = 0; i < files.length; i++)
			if (files[i].isFile())
				retStr += files[i].getName() + "\n";
		return retStr;
	}
}