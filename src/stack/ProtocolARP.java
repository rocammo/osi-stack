package stack;

import jpcap.packet.ARPPacket;
import jpcap.packet.Packet;
import util.Utils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class ProtocolARP extends Protocol {

	// Arp requests directed to our IP -> Reply with our MAC
	// Arp response directed to our MAC -> Add ARP table DONE
	// User inputs IP address -> Look up ARP table and send ARP request if missing

	// IP & ArpEntry ( MAC and timestamp )
	HashMap<String, ArpEntry> arpTable = new HashMap<String, ArpEntry>();

	public static void arpQueryApp(ProtocolARP protocolARP) {
		System.out.println("ARP Query App");
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("ARP Query App: Enter the IP to resolve (XXX.XXX.XXX.XXX): ");
		String ipStr = scanner.nextLine();
		
		// split into digits the String
		String[] ipArr = ipStr.split("\\.");
		
		// convert from String to byte[]
		byte[] ipAddr = new byte[4];

		for (int i = 0; i < 4; i++) {
			int digit = Integer.parseInt(ipArr[i]);
			ipAddr[i] = (byte) digit;
		}
		
		Layer3 network = (Layer3)protocolARP.getLowLayer();
		Layer2 datalink = (Layer2)network.getLowLayer();
		
		byte[] senderHardwarAdd = datalink.getMacAddr();
		byte[] senderProtoAdd = network.getIpAddr();
		byte[] target_protoaddr = ipAddr;
		
		ARPPacket arpRequest = ProtocolARP.generateArpRequest(senderHardwarAdd, senderProtoAdd, target_protoaddr);
		System.out.println("Layer 3: Sending ARP packet downwards");
		network.sendDownwards(arpRequest);
		
		long start = System.currentTimeMillis();
	    long end = start + 3*1000;	//3 seconds
	    while(true) {
	    	
	    	if (protocolARP.arpTable.containsKey(Utils.ipBytesToString(target_protoaddr))) {
	    		ArpEntry answer = protocolARP.arpTable.get(Utils.ipBytesToString(target_protoaddr));
	    	    System.out.println("ARP Query App: Answer received, " + Utils.ipBytesToString(target_protoaddr)
	    	    		+ " belongs to " + Utils.macBytesToString(answer.getMacAddr()));
	    	    break;
	    	}
	    	
	        if(System.currentTimeMillis() > end) {
	    		System.err.println("ARP Query App: timeout for " + Utils.ipBytesToString(target_protoaddr));
	            break;
	        }
	    }
		
	}
	
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
							System.out.println("ProtocolARP: Dropping the packet as is not directed to resolve our IP");
						}
						
						break;
						
					case ARPPacket.ARP_REPLY: 
						System.out.println("ProtocolARP: ARP REPLY ");
						
						System.out.println("ProtocolARP: Adding to ARPTable " + Utils.ipBytesToString( arpPacket.sender_protoaddr ) 
							+ " " + Utils.macBytesToString( arpPacket.sender_hardaddr ) );
						
						arpTable.put(Utils.ipBytesToString(arpPacket.sender_protoaddr),
								new ArpEntry(arpPacket.sender_hardaddr));						
						
						break;
						
					default: System.err.println("ProtocolARP: UNKNOWN ");break;
				}


			} else {
				semaphore.release();
			}
		}
	}
}