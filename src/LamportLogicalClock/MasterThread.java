
package LamportLogicalClock;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Author : Shalini Ravishankar
 * 
 * The MasterThread belong to the Master
 * 
 */

public class MasterThread implements Runnable {

	private Integer masterHost;
	private Map<String, Integer> process = new HashMap<String, Integer>();
	public int logicalClock = 0;

	ServerSocket myService;
	public static int[] port_list = new int[] { 18808, 18818, 18828, 18838 };
	public static int[] offset_process = new int[4];
	public boolean Flag = true;

	public MasterThread(Integer masterHost) {
		super();
		this.masterHost = masterHost;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (!Thread.currentThread().isInterrupted()) {

			process.put("MO", 0);
			process.put("P1", 0);
			process.put("P2", 0);
			process.put("P3", 0);
			process.put("P4", 0);

			offset_process[0] = 0;
			offset_process[1] = 0;
			offset_process[2] = 0;
			offset_process[3] = 0;

			try {
				myService = new ServerSocket(masterHost);
				++logicalClock;
				// System.out.println("Starting Master thread in port " +
				// masterHost);
				// logicalClock++;
				// System.out.println("Logical Clock Value for Master = " +
				// logicalClock );

				while (true) {

					// System.out.println("Logical Clock Value for Master = " +
					// logicalClock );
					Socket channel = myService.accept();

					/***
					 * If there are any incoming connection
					 * request,MasterConnectionHandler will be invoked.
					 * MasterConnectionHandler will take handle the
					 * communication for Master.
					 * 
					 */

					MasterConnectionHandler handler = new MasterConnectionHandler(
							channel, logicalClock, process, port_list);
					Thread masterConnectionThread = new Thread(handler);
					masterConnectionThread.start();
					// logicalClock = handler.logicalClock;

				}
			}

			catch (Exception e) {
				e.printStackTrace();

			}

		}
		if (Thread.currentThread().isInterrupted()) {
			System.exit(0);
		}

	}
}
