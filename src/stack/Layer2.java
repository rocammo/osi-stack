package stack;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

public class Layer2 extends Layer {
	private static final int MAC_LENGTH = 6;
	private byte[] macAddr = new byte[MAC_LENGTH];

	private Scanner scanner = new Scanner(System.in);

	public Layer2(byte[] macAddr) {
		this.macAddr = macAddr;
	}

	public Layer2() {
		config();
	}

	@Override
	public void config() {
		this.macAddr = requestMac();
	}

	@Override
	public void run() {
		byte[] bcastAddr = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };

		while (running) {
			try {
				lowSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (!lowQueue.isEmpty()) {
				Packet p = lowQueue.poll();
				lowSemaphore.release();

				// if the package comes from another hosts, then, modify the packet
				// by setting the destination MAC address as broadcast, so that the
				// packet can be sent to all devices on the network

				if(p != null && p.datalink != null) {
					EthernetPacket ep = (EthernetPacket) p.datalink;
					
					// Collect the packets destinated to us or to broadcast
					if (Arrays.equals(ep.dst_mac, macAddr) || Arrays.equals(ep.dst_mac, bcastAddr)) {
						sendUpwards(p);
					}else {
				//		System.out.println("Layer2: Packet discarded as not directed to us."
				//	+ Arrays.toString(ep.dst_mac));
					}
				
				
				}else {
					System.out.println("L2: Error, p.datalink was null.");
					break;
				}


			} else {
				lowSemaphore.release();
			}
			
			
			if (!topQueue.isEmpty()) {
				Packet p = topQueue.poll();
				topSemaphore.release();

				ARPPacket arpP = (ARPPacket) p;
				switch(arpP.operation){
					case ARPPacket.ARP_REQUEST: System.out.println("L2: ARP REQUEST ");break;
					case ARPPacket.ARP_REPLY: System.out.println("L2: ARP REPLY ");break;
					case ARPPacket.RARP_REQUEST: System.out.println("L2: RARP REQUEST ");break;
					case ARPPacket.RARP_REPLY: System.out.println("L2: RARP REPLY ");break;
					case ARPPacket.INV_REQUEST: System.out.println("L2: IDENTIFY REQUEST ");break;
					case ARPPacket.INV_REPLY: System.out.println("L2: IDENTIFY REPLY ");break;
					default: System.out.println("UNKNOWN ");break;
				}
				
				EthernetPacket ethP = new EthernetPacket();
				ethP.frametype=EthernetPacket.ETHERTYPE_ARP;
				ethP.src_mac=getMacAddr();
			
				
				switch(arpP.operation){
				case ARPPacket.ARP_REQUEST: 
					ethP.dst_mac=bcastAddr;
					break;
					
				case ARPPacket.ARP_REPLY:
					ethP.dst_mac=arpP.target_hardaddr;
					break;
				}
				
				arpP.datalink=ethP;
				
				System.out.println("Layer 2: Sending ARP packet downwards");
				sendDownwards(arpP);

			} else {
				lowSemaphore.release();
			}
			
		}

		while (!topLayer.hasFinished()) {
			// wait for the queues to be emptied
		}
		topLayer.close();
	}

	private byte[] requestMac() {
		System.out.print("LAYER 2: Enter the source MAC address (XX:XX:XX:XX:XX:XX): ");
		String macStr = scanner.nextLine();

		// validate the MAC address with a regular expression
		while (!isValidMac(macStr)) {
			System.out.print("LAYER 2: Enter the source MAC address (XX:XX:XX:XX:XX:XX): ");
			macStr = scanner.nextLine();
		}

		// split into digits the String
		String[] macArr = macStr.split(":");

		// convert from String to byte[]
		byte[] macAddr = new byte[MAC_LENGTH];

		for (int i = 0; i < MAC_LENGTH; i++) {
			int digit = Integer.parseInt(macArr[i], 16);
			macAddr[i] = (byte) digit;
		}

		return macAddr;
	}

	private boolean isValidMac(String macStr) {
		Pattern p = Pattern.compile("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");
		Matcher m = p.matcher(macStr);
		return m.find();
	}

	public byte[] getMacAddr() {
		return macAddr;
	}

	public void setMacAddr(byte[] macAddr) {
		this.macAddr = macAddr;
	}
}
