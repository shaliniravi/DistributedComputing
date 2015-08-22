package LamportLogicalClock;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * 
 * Author : Shalini Ravishankar
 * 
 * The ProcessConnectionHandler is responsible for Process's Communication
 * 
 */

public class ProcessConnectionHandler implements Runnable {

	public Socket channel;
	public Integer logicalClock;
	private int new_timestamp;
	private String processId;

	public ProcessConnectionHandler(Socket channel, Integer logicalClock,
			String processId) {
		super();
		this.channel = channel;
		this.logicalClock = logicalClock;
		this.processId = processId;
	}

	@Override
	public void run() {

		String value;

		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(
					channel.getInputStream()));
			while ((value = input.readLine()) != null) {

				/**
				 * 
				 * If the message starts with P means the message from a
				 * Process, because the message format will start with P for
				 * process and starts with Master for Master
				 * 
				 */

				if (value.startsWith("P")) {
					// System.out.println("value" +value);

					/**
					 * 
					 * Message will be sent as String. Sample Format = P1: My
					 * Logical Clock = 8 To get the value of logical clock, we
					 * need to get substring the message and parse it to Integer
					 * for further calculations
					 * 
					 */

					new_timestamp = Integer.parseInt(value.substring(20));

					// System.out.println(processId.toUpperCase() +
					// " : value received from Process = "
					// + value.substring(0, 2) + " -----> " +
					// value.substring(3));

					/***
					 * If the process receives a message from other process, it
					 * will get the maximum value between the logical clock
					 * value of sending process and its current logical value
					 * and add plus one( as receiving is also an event)
					 * 
					 */

					logicalClock = Math.max(logicalClock, new_timestamp) + 1;
					System.out.println("Logical Clock Value for "
							+ processId.toUpperCase() + " " + logicalClock);

				}

				else if (value.startsWith("Master")) {

					/**
					 * 
					 * Message will be sent as String. Sample Format = Offset
					 * Value = 8 To get the value of logical clock, we need to
					 * get substring the message and parse it to Integer for
					 * further calculations
					 * 
					 */

					new_timestamp = Integer.parseInt(value.substring(16));

					// System.out.println("Value from Master : Offset = " +
					// new_timestamp + " Received by "
					// + processId.toUpperCase());

					// System.out.println(processId.toUpperCase()+" :value received from master = "
					// + value.substring(3));

					/***
					 * If the process receives a message from Master, it will
					 * just add the offset value with its current logical clock
					 * and add plus one( as receiving is also an event)
					 * 
					 */

					logicalClock = logicalClock + new_timestamp + 1;
					System.out.println("Logical Clock Value for "
							+ processId.toUpperCase() + " " + logicalClock);

					/***
					 * Byzantine Failure Implementation Making the Process P1 to
					 * increase the logical clock by 5 units at a random moment
					 * 
					 * 
					 */

					int byzantine = (int) (Math.random() * 6 + 1);
					if (processId.toUpperCase() == "P1" && byzantine == 5) {
						logicalClock = logicalClock + 5;
					}

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
