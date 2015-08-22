package LamportLogicalClock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;

/**
 * 
 * Author : Shalini Ravishankar
 * 
 * The MasterConnectionHandler is responsible for Master's Communication
 * 
 */

public class MasterConnectionHandler implements Runnable {

	private Socket channel;
	public Integer logicalClock;
	public int[] port_list;
	private Map<String, Integer> process;

	/****
	 * 
	 * @param channel
	 *            - Socket channel
	 * @param logicalClock
	 *            - current logical clock value of Master
	 * @param process
	 *            - A Map which has the latest timestamp sent by each Process
	 * @param port_list
	 *            - List of Port belongs to the Processes
	 */

	public MasterConnectionHandler(Socket channel, Integer logicalClock,
			Map<String, Integer> process, int[] port_list) {
		super();
		this.channel = channel;
		this.logicalClock = logicalClock;
		this.process = process;
		this.port_list = port_list;

	}

	@Override
	public void run() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(
					channel.getInputStream()));
			String value;
			int[] offset_process = new int[4];
			int process_timestamp;
			while ((value = input.readLine()) != null) {

				int sum = 0;
				int average = 0;
				process.put("MO", logicalClock);
				// System.out.println("Logical Clock Value for Master " +
				// logicalClock );
				process_timestamp = Integer.parseInt(value.substring(20));

				if (value.startsWith("P1")) {
					process.put("P1", process_timestamp);

					// System.out.println("[MASTER]: Value received from Process 1 = "
					// + value.substring(3));

				} else if (value.startsWith("P2")) {
					process.put("P2", process_timestamp);

					// System.out.println("[MASTER]: Value received from Process 2 = "
					// + value.substring(3));

				} else if (value.startsWith("P3")) {
					process.put("P3", process_timestamp);
					// System.out.println("[MASTER]: Value received from process 3 ="
					// + value.substring(3));
				} else if (value.startsWith("P4")) {
					process.put("P4", process_timestamp);
					// System.out.println("[MASTER]: Value received from process 4 ="
					// + value.substring(3));
				}

				for (int item : process.values()) {
					sum += item;
				}

				/****
				 * Offset will calculated based on the average of all timestamps
				 * from the Map ( which has both Master's as well as all
				 * processes timestamp
				 * 
				 * Once the Offset is calculated, it will be saved in a
				 * list(offset_process) with respect to each process(index
				 * indentfies the process)
				 * 
				 * index : 0 = P1
				 * 
				 */

				average = sum / 5;
				logicalClock = average;

				System.out.println("Logical Clock Value for Master "
						+ logicalClock);

				offset_process[0] = average - process.get("P1");
				offset_process[1] = average - process.get("P2");
				offset_process[2] = average - process.get("P3");
				offset_process[3] = average - process.get("P4");

				/**
				 * Offset will be sent to all process
				 * 
				 */
				sendmessage(offset_process, port_list);

			}
		}

		catch (Exception e) {
			e.printStackTrace();

		}

	}

	private void sendmessage(int new_offset[], int[] port_list) {
		// TODO Auto-generated method stub
		try {

			/**
			 * 
			 * port_list has the port of all processes. Message will be sent to
			 * all process.
			 * 
			 */

			for (int i = 0; i < port_list.length; i++) {
				Socket processClient = new Socket("localhost", port_list[i]);
				processClient.setReuseAddress(true);
				OutputStream processStream = processClient.getOutputStream();
				BufferedWriter processWriter = new BufferedWriter(
						new OutputStreamWriter(processStream));

				processWriter.write("Master: Offset =" + new_offset[i]);

				// System.out.println("Sending data from Master to other process running on  "
				// + port_list[i]);

				processWriter.flush();
				// processWriter.close();
				processClient.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
