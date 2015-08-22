package LamportLogicalClock;

/**
 * 
 * Author : Shalini Ravishankar
 * 
 * This SocketTest class is a Simulation to resolve the clock consistency using
 * Lamport and Berkley Algorithms
 * 
 */
public class SocketTest {

	private static long start = System.currentTimeMillis();
	private static long end = start + 60 * 1000;

	public static void main(String[] args) {

		/**
		 * Instantiating MasterThread Class
		 */

		MasterThread master = new MasterThread(1984);

		Thread m1 = new Thread(master);

		/**
		 * Instantiating ProcessThread Class with 4 process objects
		 */
		ProcessThread p1 = new ProcessThread();
		p1.eventInterval = 2000;
		p1.machineName = "localhost";
		p1.masterHost = 1984;
		p1.processId = "P1";
		p1.myHost = 18808;

		ProcessThread p2 = new ProcessThread();
		p2.eventInterval = 4000;
		p2.machineName = "localhost";
		p2.masterHost = 1984;
		p2.processId = "P2";
		p2.myHost = 18818;

		ProcessThread p3 = new ProcessThread();
		p3.eventInterval = 8000;
		p3.machineName = "localhost";
		p3.masterHost = 1984;
		p3.processId = "P3";
		p3.myHost = 18828;

		ProcessThread p4 = new ProcessThread();
		p4.eventInterval = 10000;
		p4.machineName = "localhost";
		p4.masterHost = 1984;
		p4.processId = "P4";
		p4.myHost = 18838;

		/**
		 * Starting master
		 */

		m1.start();

		/**
		 * Starting process
		 */

		Thread process1 = new Thread(p1);
		process1.start();

		Thread process2 = new Thread(p2);
		process2.start();

		Thread process3 = new Thread(p3);
		process3.start();

		Thread process4 = new Thread(p4);
		process4.start();

		/**
		 * Killing the process if the time has been reached the maximum
		 */

		if (System.currentTimeMillis() == end) {
			m1.interrupt();
			process1.interrupt();
			process2.interrupt();
			process3.interrupt();
			process4.interrupt();

		}
	}
}