package stack;

import jpcap.packet.ARPPacket;
import jpcap.packet.Packet;
import java.util.HashMap;

public class ProtocolARP extends Protocol {
	
	//IP, MAC (and timestamp)
    HashMap<byte[], ArpEntry> arpTable = new HashMap<byte[], ArpEntry>();
	
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
				
				byte[] bcastAddr = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
				if( arpPacket.getTargetHardwareAddress() == bcastAddr) {
					//Is arp request
					
					//Here the app has to compare the destination IP and if its its own, reply
					//if(arpPacket.getTargetProtocolAddress() == LAYER3.ipAddr){
					//		//Create and send an ARP answer to the network.
					//}else{
					//		//We drop the packet, as we cannot answer it
					//}
					
				}else {
					//Is arp answer
					arpTable.put(arpPacket.sender_protoaddr, new ArpEntry(arpPacket.sender_hardaddr));
					//We drop the packet
				}
				
				
				System.out.println("ARP PACKET: " + arpPacket);
			} else {
				semaphore.release();
			}
		}
	}
}
