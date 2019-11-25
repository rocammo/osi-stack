package stack;

import jpcap.packet.ARPPacket;
import jpcap.packet.Packet;
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

				byte[] bcastAddr = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
				if (arpPacket.getTargetHardwareAddress() == bcastAddr) {

					// We check the destination IP and if its its own, reply
					if (arpPacket.getTargetProtocolAddress() == ((Layer3) getLowLayer()).getIpAddr()) {
						// Arp requests directed to our IP -> Reply with our MAC
						ARPPacket arpResponse = generateArpResponse((byte[]) arpPacket.getSenderHardwareAddress(),
								((Layer3) getLowLayer()).getIpAddr(),
								((Layer2) getLowLayer().getLowLayer()).getMacAddr());
						System.out.println("ProtocolARP: Sending ARP repsonse.");
						getLowLayer().sendDownwards(arpResponse);
					} // Else we drop the packet, as we cannot answer it

				} else {
					// Arp response directed to our MAC -> Add ARP table
					System.out.println("ProtocolARP: Added to ARPTable - " + arpPacket.sender_protoaddr + " - "
							+ arpPacket.sender_hardaddr);
					arpTable.put(arpPacket.sender_protoaddr, new ArpEntry(arpPacket.sender_hardaddr));
				}

				// System.out.println("ARP PACKET: " + arpPacket);
			} else {
				semaphore.release();
			}
		}
	}
}