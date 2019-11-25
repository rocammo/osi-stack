package stack;

import jpcap.packet.IPPacket;
import jpcap.packet.Packet;

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
				Packet p = packets.poll();
				semaphore.release();
				IPPacket ipPacket = (IPPacket) p;
				//Drop as we dont handle this type of packets
				
			} else {
				semaphore.release();
			}
		}
	}
}
