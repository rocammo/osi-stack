package stack;

import jpcap.packet.ARPPacket;
import jpcap.packet.Packet;

import java.util.Arrays;
import java.util.HashMap;

public class ProtocolARP extends Protocol {

	// Arp requests directed to our IP -> Reply with our MAC
	// Arp response directed to our MAC -> Add ARP table DONE
	// User inputs IP address -> Look up ARP table and send ARP request if missing

	// IP & ArpEntry ( MAC and timestamp )
	HashMap<byte[], ArpEntry> arpTable = new HashMap<byte[], ArpEntry>();

	public static ARPPacket generateArpRequest(byte[] senderHardwarAdd, byte[] senderProtoAdd,
			byte[] target_protoaddr) {
		byte[] broadcast = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
		ARPPacket arp = new ARPPacket();
		arp.hardtype = ARPPacket.HARDTYPE_ETHER;
		arp.prototype = ARPPacket.PROTOTYPE_IP;
		arp.operation = ARPPacket.ARP_REQUEST;
		arp.hlen = 6;
		arp.plen = 4;
		arp.sender_hardaddr = senderHardwarAdd;
		arp.sender_protoaddr = senderProtoAdd;
		arp.target_hardaddr = broadcast;
		arp.target_protoaddr = target_protoaddr;

		return arp;

//		EthernetPacket ether=new EthernetPacket();
//		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
//		ether.src_mac=X;
//		ether.dst_mac=broadcast;
//		arp.datalink=ether;
//		
//		sender.sendPacket(arp);
	}

	public static ARPPacket generateArpResponse(byte[] senderHardwarAdd, byte[] senderProtoAdd,
			byte[] target_hardaddr) {
		byte[] broadcast = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
		ARPPacket arp = new ARPPacket();
		arp.hardtype = ARPPacket.HARDTYPE_ETHER;
		arp.prototype = ARPPacket.PROTOTYPE_IP;
		arp.operation = ARPPacket.ARP_REPLY;
		arp.hlen = 6;
		arp.plen = 4;
		arp.sender_hardaddr = senderHardwarAdd;
		arp.sender_protoaddr = senderProtoAdd;
		arp.target_hardaddr = target_hardaddr;

		return arp;

//		EthernetPacket ether=new EthernetPacket();
//		ether.frametype=EthernetPacket.ETHERTYPE_ARP;
//		ether.src_mac=X;
//		ether.dst_mac=broadcast;
//		arp.datalink=ether;
//		
//		sender.sendPacket(arp);
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
				
				System.out.println("Hay paquetico en ProtocolARP, haciendo cosas...");
				
				switch(arpPacket.operation){
					case ARPPacket.ARP_REQUEST: System.out.println("ARP REQUEST ");break;
					case ARPPacket.ARP_REPLY: System.out.println("ARP REPLY ");break;
					case ARPPacket.RARP_REQUEST: System.out.println("RARP REQUEST ");break;
					case ARPPacket.RARP_REPLY: System.out.println("RARP REPLY ");break;
					case ARPPacket.INV_REQUEST: System.out.println("IDENTIFY REQUEST ");break;
					case ARPPacket.INV_REPLY: System.out.println("IDENTIFY REPLY ");break;
					default: System.out.println("UNKNOWN ");break;
				}
			
			
				switch(arpPacket.operation){
					case ARPPacket.ARP_REQUEST:
						System.out.println("ProtocolARP: ARP REQUEST ");
						
						if (Arrays.equals(arpPacket.target_protoaddr, (((Layer3) getLowLayer()).getIpAddr()))) {
							// Arp requests directed to our IP -> Reply with our MAC
							ARPPacket arpResponse = generateArpResponse( arpPacket.sender_hardaddr,
									((Layer3) getLowLayer()).getIpAddr(),
									((Layer2) getLowLayer().getLowLayer()).getMacAddr());
							System.out.println("ProtocolARP: Sending ARP response.");
							getLowLayer().sendDownwards(arpResponse);
							
						}else {
							// Else we drop the packet, as we cannot answer it
							System.out.println("ProtocolARP: TARGET IP no es nuestra, dropeo");
						}
						
						break;
						
					case ARPPacket.ARP_REPLY: 
						System.out.println("ProtocolARP: ARP REPLY ");
						
						// Arp reply -> Add ARP table
						System.out.println("ProtocolARP: Added to ARPTable - " + Arrays.toString(arpPacket.sender_protoaddr) + " - "
								+ Arrays.toString(arpPacket.sender_hardaddr));
						arpTable.put(arpPacket.sender_protoaddr, new ArpEntry(arpPacket.sender_hardaddr));
						
						break;
						
					default: System.out.println("ProtocolARP: UNKNOWN ");break;
				}


			} else {
				semaphore.release();
			}
		}
	}
}