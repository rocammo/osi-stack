package stack;

public class ProtocolIP extends Protocol {
	@Override
	public void run() {
		while (running) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (!packets.isEmpty()) {
				packets.poll();
				semaphore.release();
				//Drop as we dont handle this type of packets
				
			} else {
				semaphore.release();
			}
		}
	}
}
