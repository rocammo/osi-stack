package stack;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import util.Utils;

public class ProtocolICMP extends Protocol {
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
				// Drop as we dont handle this type of packets

			} else {
				semaphore.release();
			}
		}
	}
	
	public static void pingSenderApp(Layer3 network) {

		 while (true) {
			try {
				TimeUnit.SECONDS.sleep(3); // Wait between Pings
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Scanner scanner = new Scanner(System.in);

			System.out.println("Ping Sender App: Enter the IP to ping (XXX.XXX.XXX.XXX): ");
			String ipStr = scanner.nextLine();

			// split into digits the String
			String[] ipArr = ipStr.split("\\.");

			// convert from String to byte[]
			byte[] ipAddr = new byte[4];

			for (int i = 0; i < 4; i++) {
				int digit = Integer.parseInt(ipArr[i]);
				ipAddr[i] = (byte) digit;
			}

			ProtocolICMP.sendICMP(network, ipAddr);
			
		}

	}
	
	public static void sendICMP(Layer3 network, byte[] ipAddr) {
		byte[] destinationHardwarAdd;
		
		if( network.isLocal(ipAddr) ) {
			System.out.println("sendICMP: Destination ip is local to our network.");
			destinationHardwarAdd = ProtocolARP.resolveIP(network.getProtocolARP(), ipAddr);
			
		}else {
			System.out.println("sendICMP: Destination ip is outside from our network.");
			destinationHardwarAdd = ProtocolARP.resolveIP(network.getProtocolARP(), network.getIpGateway());
		}
		
		if(destinationHardwarAdd == null) {
			System.out.println("sendICMP: IP is not online, no ARP response given." );
		}else {
			System.out.println( Utils.macBytesToString(destinationHardwarAdd) );

		    ICMPPacket p=new ICMPPacket();
		    p.type=ICMPPacket.ICMP_ECHO;
		    p.seq=1000;
		    p.id=999;
		    p.orig_timestamp=123;
		    p.trans_timestamp=456;
		    p.recv_timestamp=789;
		    try {
				p.setIPv4Parameter(0,false,false,false,0,false,false,false,0,1010101,100,IPPacket.IPPROTO_ICMP, InetAddress.getByName(Utils.ipBytesToString(network.getIpAddr())),InetAddress.getByName(Utils.ipBytesToString(ipAddr)));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    p.data="Pruebita".getBytes();

		    /** EthernetPacket ether=new EthernetPacket();
		    ether.frametype=EthernetPacket.ETHERTYPE_IP;
		    ether.src_mac=new byte[]{(byte)00,(byte)26,(byte)18,(byte)00,(byte)25,(byte)65};    
		    ether.dst_mac=new byte[]{(byte)90,(byte)230,(byte)186,(byte)60,(byte)205,(byte)90};
		    p.datalink=ether;*/
		    
		    System.out.println("sendICMP: Sending to layer2...");
		    network.sendDownwards(p);

		}
		
		
	}
}
