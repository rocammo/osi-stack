package stack;

import jpcap.packet.ARPPacket;

public class ProtocolARP extends Protocol {
	@Override
	public void run() {
		while (true) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (!packets.isEmpty()) {
				ARPPacket arpPacket = (ARPPacket) packets.poll();
				System.out.println("ARP PACKET: " + arpPacket);	
			} else {
				semaphore.release();
			}
		}
	}
}
