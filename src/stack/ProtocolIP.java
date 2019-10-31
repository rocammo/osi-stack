package stack;

import jpcap.packet.IPPacket;

public class ProtocolIP extends Protocol {
	@Override
	public void run() {
		while (true) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (!packets.isEmpty()) {
				IPPacket ipPacket = (IPPacket) packets.poll();
				System.out.println("IP PACKET: " + ipPacket);
			} else {
				semaphore.release();
			}
		}
	}
}
