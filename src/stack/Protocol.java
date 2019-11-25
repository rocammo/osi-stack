package stack;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import jpcap.packet.Packet;

public abstract class Protocol extends Thread {
	boolean running = true;
	Layer lowLayer;

	Queue<Packet> packets = new LinkedList<Packet>(); // only 1 thread can access
	Semaphore semaphore = new Semaphore(1); // the resource at any one time.

	public abstract void run();

	public void close() {
		System.err.println("Closing " + getClass().getName());
		this.running = false;
	}
	
	public boolean hasFinished() {
		return (packets.isEmpty()) ? true : false;
	}
	
	public Layer getLowLayer() {
		return lowLayer;
	}

	public void setLowLayer(Layer lowLayer) {
		this.lowLayer = lowLayer;
	}
}
