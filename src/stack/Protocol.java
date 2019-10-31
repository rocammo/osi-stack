package stack;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import jpcap.packet.Packet;

public abstract class Protocol extends Thread {
	Queue<Packet> packets = new LinkedList<Packet>(); // only 1 thread can access
	Semaphore semaphore = new Semaphore(1); 		  // the resource at any one time.
	
	public abstract void run();
}
