package EncryptedFileTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * @author Shalini Ravishankar
 * 
 *         FileConnectionHandler : The Server thread responsible for file
 *         transfer with the Client. It is responsible for the interprocess
 *         communication
 * 
 */

public class FileConnectionHandler implements Runnable {

	private Socket channel;
	BufferedReader input = null;
	private String filetosend;
	private String fileName;
	private String userName;
	private String pwd;
	FileInputStream finput = null;
	File file;
	private int flag = 0;
	long CheckValue;
	Socket client;

	private static Integer CPORT;
	private static String machineName;
	String clientHost;

	/*
	 * path : The Source path where the file are stored in the server
	 * Credentials : Holds the user name and password of the Clients. It is a
	 * reference of Server's Credentials
	 */

	private String path = "/home/sravisha/ServerFiles/";
	Map<String, String> Credentials = new HashMap<String, String>();

	public FileConnectionHandler(Socket channel, Map<String, String> Credentials) {
		super();
		this.channel = channel;
		this.Credentials = Credentials;
	}

	@Override
	public void run() {

		try {

			String info;

			boolean Authentication;

			// clientHost holds the IP address of Client
			machineName = channel.getRemoteSocketAddress().toString()
					.substring(1);
			String a[] = machineName.split(":");
			clientHost = a[0];
			System.out.println("Client's Ip Address :" + clientHost);

			/*
			 * info : Reads the input from the Client. The Client sends their
			 * credentials along with the filename in the format. eg.
			 * UserName|userName|passWord|fileName|Port number
			 */

			while (true) {

				input = new BufferedReader(new InputStreamReader(
						channel.getInputStream()));
				info = input.readLine();

				if (info.startsWith("User")) {

					String tempStr[] = info.split("\\|");
					userName = tempStr[1];
					pwd = tempStr[2];

					// Authenticates the Client
					Authentication = authenticateUser(userName, pwd);
					if (Authentication) {

						fileName = tempStr[3];

						// CPORT holds the Client's Port number
						CPORT = Integer.parseInt(tempStr[4]);

						System.out.println("FileName Recieved from Client: "
								+ fileName);

						/*
						 * If the Client has been authenticated then proceeds
						 * forward by accepting the request else close the
						 * communication.
						 */

						// filetosend; holds the file location

						filetosend = path + fileName;
						file = new File(filetosend);
						boolean isFile;

						/*
						 * If the requested file is in the server then it will
						 * send the encrypted version of the file to the client
						 * else it will send an error message and close the
						 * connection.
						 */

						isFile = CheckFile(file);
						if (isFile) {

							CheckValue = generateChecksum(file);
							SendFile(file);

						}

						else {

							input.close();
							channel.close();
							break;
						}

					} else {

						System.out
								.println("Given Credentials are not matching");
						input.close();
						channel.close();
						break;
					}
				}

				else {
					boolean isTransfer;
					String check = info;
					isTransfer = CheckTransfer(CheckValue, check);
					System.out.println("Checksum recieved from Client");

					/*
					 * This Check will ensure that the file transfer has been
					 * done successfully. If not then the task will be
					 * automatically retried for 5 times till the file has been
					 * sent successfully.
					 */

					if (isTransfer == true && flag < 5) {
						flag++;
						SendFile(file);

					}
					if (isTransfer == false && flag < 5) {

						System.out
								.println("File transfered Succesfully (Checksum are identical)");
						OutputStream op2 = channel.getOutputStream();
						op2.write(3);
						op2.flush();
						break;
					}
					if (flag > 5) {

						System.out
								.println("Sorry File not transfered Succesfully");
						OutputStream op3 = channel.getOutputStream();
						op3.write(2);
						op3.flush();
						break;
					}

				}
			}
		} catch (IOException e) {

			System.out.println("Error in Connection");
		} catch (Throwable e) {

			System.out.println("Error in Connection ");
		}

	}

	/*
	 * authenticateUser: The Client's credentials will be registered for the
	 * first time(Client's connection) The user name and password will be stored
	 * in server for authenticate the client's further sessions.
	 */

	private boolean authenticateUser(String userName, String pwd)
			throws IOException {

		/*
		 * If the Client has been already registered, it will check the user
		 * name and password are same as the ones stored in server.
		 */

		if (Credentials.containsKey(userName)) {

			String password = (String) Credentials.get(userName);

			if (password.equals(pwd)) {
				return true;
			}

			else {
				OutputStream op5 = channel.getOutputStream();
				op5.write(1);
				op5.flush();
				op5.close();
				return false;
			}

		}

		else {

			// register the client's credentials with server
			Credentials.put(userName, pwd);
			return true;
		}

	}

	/*
	 * CheckFile: If the requested file is in the server then it will process
	 * forward else it will send an error message and close the connection.
	 */

	private boolean CheckFile(File file) throws IOException {

		if (file.isFile()) {

			System.out.println("File Exists");
			OutputStream op = channel.getOutputStream();
			op.write(4);
			op.flush();
			return true;
		}

		else {

			System.out.println("File not Exists on the Server");
			OutputStream op = channel.getOutputStream();
			op.write(0);
			op.flush();
			op.close();
			return false;
		}

	}

	/*
	 * SendFile: Creates a socket to send the file to Client.
	 */

	private void SendFile(File file2) throws Throwable, IOException {
		Socket request = new Socket(clientHost, CPORT);
		finput = new FileInputStream(file2);
		OutputStream output = request.getOutputStream();
		sendEncryptedData(output, finput, file2.length());
		request.close();

	}

	/*
	 * sendEncryptedData: If the requested file is in the server then it will
	 * process forward else it will send an error message and close the
	 * connection.
	 */
	private void sendEncryptedData(OutputStream op,
			FileInputStream filetoprocess, long l) throws Throwable {

		/*
		 * For encryption, DES algorithm is being used. CipherOutputStream will
		 * encrypt the file and send it to the Client
		 */

		String key = "shalini234567892341";
		DESKeySpec desKeySpec = new DESKeySpec(key.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey sKey = keyFactory.generateSecret(desKeySpec);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, sKey);
		CipherOutputStream cipherOut = new CipherOutputStream(op, cipher);

		InputStream fileReader = new BufferedInputStream(filetoprocess);
		int bytesRead;

		byte[] fileBuffer = new byte[(int) l];

		while ((bytesRead = fileReader.read(fileBuffer)) != -1) {
			cipherOut.write(fileBuffer, 0, bytesRead);
		}
		cipherOut.flush();
		cipherOut.close();
		op.flush();
		fileReader.close();
		filetoprocess.close();
		System.out.println("File sent");

	}

	/*
	 * generateChecksum: Calculate the checksum value for the file being
	 * transferred. CRC algorithm is used for generating the checksum
	 */

	private long generateChecksum(File fname) throws IOException {

		CheckedInputStream checksum = new CheckedInputStream(
				new FileInputStream(fname), new CRC32());
		BufferedInputStream binput = new BufferedInputStream(checksum);
		while (binput.read() != -1) {
			// Calculate the checksum for the entire file
		}
		System.out.println("Checksum calculated by server = "
				+ checksum.getChecksum().getValue());
		return checksum.getChecksum().getValue();
	}

	/*
	 * CheckTransfer: Checks weather the file has been transferred successfully
	 * based on the checksum value received from the client
	 */

	private boolean CheckTransfer(long Value, String temp) throws IOException {

		// Client will send its checksum in the format: Checksum Value=Value
		String tempStr[] = temp.split("=");
		long lvalue = Long.parseLong(tempStr[1]);

		if (lvalue == Value) {
			return false;
		} else {
			return true;
		}
	}
}
