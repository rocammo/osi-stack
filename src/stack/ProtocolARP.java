package stack;

import jpcap.packet.ARPPacket;
import jpcap.packet.Packet;
import java.util.HashMap;

public class ProtocolARP extends Protocol {
	
	//IP, MAC (and timestamp)
    HashMap<byte[], ArpEntry> arpTable = new HashMap<byte[], ArpEntry>();
	
	public static void generateArpRequest(){
		byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
		ARPPacket arp=new ARPPacket();
		arp.hardtype=ARPPacket.HARDTYPE_ETHER;
		arp.prototype=ARPPacket.PROTOTYPE_IP;
		arp.operation=ARPPacket.ARP_REQUEST;
		arp.hlen=6;
		arp.plen=4;
		arp.sender_hardaddr=X;
		arp.sender_protoaddr=X;
		arp.target_hardaddr=broadcast;
		arp.target_protoaddr=X;
		
		EthernetPacket ether=new EthernetPacket();
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac=X;
		ether.dst_mac=broadcast;
		arp.datalink=ether;
		
		sender.sendPacket(arp);
	}
	
	public static void generateArpResponse(){
		byte[] broadcast=new byte[]{(byte)255,(byte)255,(byte)255,(byte)255,(byte)255,(byte)255};
		ARPPacket arp=new ARPPacket();
		arp.hardtype=ARPPacket.HARDTYPE_ETHER;
		arp.prototype=ARPPacket.PROTOTYPE_IP;
		arp.operation=ARPPacket.ARP_REPLY;
		arp.hlen=6;
		arp.plen=4;
		arp.sender_hardaddr=X;
		arp.sender_protoaddr=X;
		arp.target_hardaddr=broadcast;
		arp.target_protoaddr=X;
		
		EthernetPacket ether=new EthernetPacket();
		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
		ether.src_mac=X;
		ether.dst_mac=broadcast;
		arp.datalink=ether;
		
		sender.sendPacket(arp);
	}
	
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
					//
					//		generateArpResponse(DESTINATION_MAC, LAYER3.ipAddr)
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
