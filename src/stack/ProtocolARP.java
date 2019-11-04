package stack;

import jpcap.packet.ARPPacket;
import jpcap.packet.Packet;

public class ProtocolARP extends Protocol {
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
				ARPPacket arpPacket = (ARPPacket) p;
				System.out.println("ARP PACKET: " + arpPacket);
			} else {
				semaphore.release();
			}
		}
	}
}
