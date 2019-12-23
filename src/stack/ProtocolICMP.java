package stack;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
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
				// Handle incoming ICMPs
				Packet p = packets.poll();
				semaphore.release();
				IPPacket ipPacket = (IPPacket) p;

				if(ipPacket instanceof ICMPPacket) {
					ICMPPacket icmpPacket = (ICMPPacket) ipPacket;
					System.out.println("ICMP Protocol: Received PONG from " + Utils.ipBytesToString(ipPacket.dst_ip.getAddress()) + ", details below:");
					System.out.println(icmpPacket);
				}else {
					//IP packet but not ICMP
				}

			} else {
				semaphore.release();
			}
		}
	}
	
	public static void pingSenderApp(Layer3 network) {

		 while (true) {
			try {
				TimeUnit.SECONDS.sleep(4); // Wait between Pings
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
		    p.data="abcdefghijklmnopqrstuvwabcdefghi".getBytes();
		    
		    System.out.println("sendICMP: Sending to layer2...");
		    network.sendDownwards(p);
	}
}
