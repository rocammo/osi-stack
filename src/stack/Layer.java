/* OSI STACK
                   +---------------+
(NULL)  topLayer   |               | <-----+ topQueue / topSemaphore  (NULL)
                   |      ...      |
                   |               |
                 ^ +---------------+
        topLayer | |               | <-----+ topQueue / topSemaphore
                 | |    LAYER 2    |
        lowLayer | |               | <-----+ lowQueue / lowSemaphore
                 v +---------------+
        topLayer ^ |               | <-----+ topQueue / topSemaphore
                 | |    LAYER 1    |
(NULL)  lowLayer | |               | <-----+ lowQueue / lowSemaphore  (NULL)
                 + +---------------+
 */

package stack;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import jpcap.packet.Packet;

public abstract class Layer extends Thread {
	Layer topLayer;
	Layer lowLayer;
	
	Queue<Packet> topQueue = new LinkedList<Packet>();
	Queue<Packet> lowQueue = new LinkedList<Packet>();
	
	Semaphore topSemaphore = new Semaphore(1);  // only 1 thread can access
	Semaphore lowSemaphore = new Semaphore(1);  // the resource at any one time.
	
	public abstract void config();
	public abstract void run();
	
	public void sendUpwards(Packet p) {
		try {
			topLayer.lowSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		topLayer.lowQueue.add(p);
		
		topLayer.lowSemaphore.release();
	}
	
	public void sendDownwards(Packet p) {
		try {
			lowLayer.topSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		lowLayer.topQueue.add(p);
		
		lowLayer.topSemaphore.release();
	}
	
	public Layer getTopLayer() {
		return topLayer;
	}
	public void setTopLayer(Layer topLayer) {
		this.topLayer = topLayer;
	}
	public Layer getLowLayer() {
		return lowLayer;
	}
	public void setLowLayer(Layer lowLayer) {
		this.lowLayer = lowLayer;
	}
}
