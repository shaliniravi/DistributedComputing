package EncryptedFileTransfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shalini Ravishankar
 * 
 *         fileServer : The Server Class responsible for Creating separate
 *         Threads for Each Clients, thereby allowing Multiple Concurrent
 *         requests.
 * 
 */

public class fileServer {

	private static final int PORT_NUMBER = 11111;

	/*
	 * Credentials is Saves the user name and password of Clients. Register the
	 * Clients at the first time and use them for authenticate the client for
	 * further sessions.
	 */

	Map<String, String> Credentials = new HashMap<String, String>();

	ServerSocket myService;

	public fileServer() {
		try {
			myService = new ServerSocket(PORT_NUMBER);
			System.out.println("Server Started");
		} catch (IOException ioe) {
			System.out.println("Could not Create Server Socket.");
			System.exit(-1);
		}

		/*
		 * The server Listens for Connection
		 */
		while (true) {

			try {
				System.out.println("Connection Established in Server side");
				Socket channel = myService.accept();
				System.out.println("Accepted connection from : "
						+ channel.getPort());

				/*
				 * If there are any incoming connection
				 * request,FileConnectionHandler will be invoked.
				 * FileConnectionHandler will handle the communication of Server
				 */
				FileConnectionHandler handler = new FileConnectionHandler(
						channel, Credentials);
				Thread serverConnectionThread = new Thread(handler);
				serverConnectionThread.start();

			}

			catch (Exception e) {
				System.out.println("Connection Error");

			}
		}

	}

	public static void main(String[] args) {
		new fileServer();
	}

}
