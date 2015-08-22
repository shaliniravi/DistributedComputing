package EncryptedFileTransfer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * @author Shalini Ravishankar
 * 
 *         fileClient : The Client Class responsible for client side
 *         communication
 * 
 */

public class fileClient {

	private static Integer serverHost = 11111;
	private static Integer myPort;
	private static String machineName;

	private static PrintWriter output = null;
	private static String fileName;
	private static String filetoReceive;

	// credentials of Client
	private static String userName;
	private static String password;
	
	//destination path
	static String path = "/home/sravisha/ClientFiles/";
	long checksumValue;
	static File file;
	static ServerSocket clientsoc;

	public static void main(String args[]) throws Throwable {

		Socket request;

		try {

			//Requesting connection to Server's IP :10.234.140.29
			request = new Socket("10.234.140.29", serverHost);
			System.out.println("Sending Request to Server");

			// Get the User name, Password , Port and fileName to be transferred from the server

			Scanner in = new Scanner(System.in);;
			System.out.println("Enter User Name \n");
			userName = in.next();
			System.out.println("Enter Password \n");
			password = in.next();
			System.out.println("Enter the file name to receive from server \n");
			fileName = in.next();
			System.out.println("Enter Port Number \n");
			myPort = in.nextInt();
			System.out.println("Potnumber = " +myPort);


			/*
			 * Client send the credentials along with file name in the format:
			 * UserName|userName|passWord|fileName|Port number
			 * 
			 */

		    filetoReceive = path + fileName;
			output = new PrintWriter(request.getOutputStream(), true);
			String info = "UserName" + "|" + userName + "|" + password + "|"+ fileName + "|"+ myPort;
			output.write(info + '\n');
			output.flush();
			
			int temp;
			InputStream in1 = request.getInputStream();

			while (true) {
				

				temp = in1.read();

				/*
				 * Server sends the error message(0) if the file does not
				 * exist. If the file exists, client will receive the encrypted
				 * file
				 */

				if (temp == 0) {

					System.out.println("File not Exists on the server");
					break;

				}

				else if (temp == 1) {

					System.out.println("Authentication failed");
					break;

				}

				else if (temp == 2) {
  
				    //deletes the corrupted file received
					File file2 = new File(filetoReceive);
				    file2.delete();
					in1.close();
					output.close();
					request.close();
					System.out.println("File not received properly");
					System.out.println("The Corrupted file has been deleted");
					break;
				}

				else if (temp == 3) {

					System.out.println("File Recieved Succesfully");
					in1.close();
					output.close();
					request.close();
					break;

				}

				else if (temp == 4) {

					/*
					 * Receive the encrypted file and decrypt the file and save
					 * it in the destination path on the client.Created a socket to receive the file
					 */
 
					clientsoc = new ServerSocket(myPort);
					file = new File(filetoReceive);
					Socket Clientchannel = clientsoc.accept();
					
					receiveFile(Clientchannel.getInputStream(), file, output);
					Clientchannel.close();

					output.write("CheckSum Value =" + generateChecksum(file) + '\n');
					output.flush();
					System.out.println("Checksum sent to server");

				}
			}
		} catch (Exception e) {

			System.out.println("Connection Error");
		}
	}

	/*
	 * receiveFile: Decrypt the file being received
	 */

	private static void receiveFile(InputStream ip, File fname,
			PrintWriter output2) throws Throwable {

		/*
		 * For decryption, DES algorithm is being used. CipherInputStream will
		 * decrypt the file and store it in the Client's destination path
		 */

		String key = "shalini234567892341";
		FileOutputStream foutput = new FileOutputStream(fname);

		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey sKey = keyFactory.generateSecret(desKeySpec);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.DECRYPT_MODE, sKey);
		CipherInputStream cipherIn = new CipherInputStream(ip, cipher);

		byte[] fileBuffer = new byte[22118400];

		int bytesRead;
		while ((bytesRead = cipherIn.read(fileBuffer)) != -1) {
			foutput.write(fileBuffer, 0, bytesRead);
		}
		foutput.flush();
		foutput.close();
		//ip.close();
		//cipherIn.close();
		System.out.println("File received");


	}

	/*
	 * generateChecksum: Calculate the checksum value for the file received. CRC
	 * algorithm is used for generating the checksum
	 */

	private static long generateChecksum(File fname) throws Throwable {


		FileInputStream fp = new FileInputStream(fname);

		CheckedInputStream checksum = new CheckedInputStream(fp, new CRC32());
		BufferedInputStream binput = new BufferedInputStream(checksum);

		while (binput.read() != -1) {
			// Calculate the checksum for the entire file
		}

		System.out.println("Checksum is " + checksum.getChecksum().getValue());
		long cvalue = checksum.getChecksum().getValue();
		return cvalue;

	}

}
