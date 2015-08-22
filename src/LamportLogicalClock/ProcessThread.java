
package LamportLogicalClock;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 
 * Author : Shalini Ravishankar
 * 
 * The ProcessThread is responsible for Processes's Communication (Sending
 * message to other peers or Master)
 * 
 */

public class ProcessThread implements Runnable {

	public List<Integer> otherProcess;
	public Integer masterHost;
	public Integer eventInterval;
	public Integer logicalClock = 0;
	public String machineName;
	public Integer myHost;
	public String processId;
	ServerSocket myService;
	public int new_timestamp;
	public boolean Flag = true;
	public int[] port_list = new int[] { 18808, 18818, 18828, 18838 };

	// //To get a list of Candidate Processes for sending the message
	private List<Integer> getProcessList(int port) {
		List<Integer> result = new ArrayList<Integer>();
		for (int item : port_list)
			if (item != port)
				result.add(item);
		return result;
	}

	// To get a random Process number for sending a message
	private int random_process(List<Integer> process_list) {
		Random randomGenerator = new Random();
		int index = randomGenerator.nextInt(process_list.size());
		int randomItem = process_list.get(index);
		return randomItem;

	}

	@Override
	public void run() {

		while (!Thread.currentThread().isInterrupted()) {

			System.out.println("Starting Process Thread " + processId);

			/*
			 * innerServer is the server thread for the process. It will take
			 * care of server side communication for the process.
			 */

			Thread innerServer = new Thread(new Runnable() {

				@Override
				public void run() {
					try {

						myService = new ServerSocket(myHost);
						logicalClock++;
						System.out.println("Logical Clock Value for "
								+ processId.toUpperCase() + " " + logicalClock);

						while (true) {

							/***
							 * If there are any incoming connection
							 * request,ProcessConnectionHandler will be invoked.
							 * ProcessConnectionHandler will take handle the
							 * server side computation for Process.
							 * 
							 */
							Socket channel = myService.accept();
							ProcessConnectionHandler handler = new ProcessConnectionHandler(
									channel, logicalClock, processId);
							Thread handlerThread = new Thread(handler);
							handlerThread.start();

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			innerServer.start();

			long timeRefreshed = System.currentTimeMillis();
			while (true) {
				try {

					/**
					 * 
					 * eventInterval will vary for each process. This is the
					 * time interval for the process to send mesage to Master
					 * 
					 */

					if (System.currentTimeMillis() - timeRefreshed > eventInterval) {

						Socket masterClient = new Socket(machineName,
								masterHost);
						masterClient.setReuseAddress(true);
						OutputStream stream = masterClient.getOutputStream();
						BufferedWriter writer = new BufferedWriter(
								new OutputStreamWriter(stream));

						writer.write(processId + ":My logical Clock="
								+ logicalClock);
						// System.out.println("********Sending Clock Value to Master **********");

						writer.flush();
						// writer.close();
						masterClient.close();
						logicalClock++;
						System.out.println("Logical Clock Value for "
								+ processId.toUpperCase() + " " + logicalClock);

						double p = (Math.random() * (1.0 - 0.0));

						if (p <= 0.5) {

							/**
							 * Getting the port number for the random peer
							 * process
							 */
							int port_number = random_process(getProcessList(myHost));
							Socket processClient = new Socket(machineName,
									port_number);
							processClient.setReuseAddress(true);
							OutputStream processStream = processClient
									.getOutputStream();
							BufferedWriter processWriter = new BufferedWriter(
									new OutputStreamWriter(processStream));

							/**
							 * write to a random process
							 */
							processWriter.write(processId
									+ ":My logical Clock=" + logicalClock);

							// System.out.println(processId.toUpperCase()+":Sending data from "
							// + processId + " to other process running on"
							// + port_number);

							processWriter.flush();
							// processWriter.close();
							processClient.close();
							logicalClock++;
							System.out.println("Logical Clock Value for "
									+ processId.toUpperCase() + " "
									+ logicalClock);

							/**
							 * refreshing the time to track the time interval
							 * for sending message to master
							 */

							timeRefreshed = System.currentTimeMillis();
							
							
						
						}

					}
				} catch (IOException e) {

					e.printStackTrace();
				}

			}

		}

		if (Thread.currentThread().isInterrupted()) {
			System.exit(0);
		}
	}
}
