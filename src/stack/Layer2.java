package stack;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
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

				if (p != null && p.datalink != null) {
					EthernetPacket ep = (EthernetPacket) p.datalink;

					// Collect the packets destinated to us or to broadcast
					if (Arrays.equals(ep.dst_mac, macAddr) || Arrays.equals(ep.dst_mac, bcastAddr)) {
						// System.out.println("Layer 2: New packet directed to our MAC or Broadcast,
						// sending upwards (Details below)");
						// System.out.println(p);
						sendUpwards(p);
					}

				} else {
					System.err.println("Layer 2: Error, p.datalink was null.");
					break;
				}

			} else {
				lowSemaphore.release();
			}

			if (!topQueue.isEmpty()) {
				Packet p = topQueue.poll();
				topSemaphore.release();

				if(p instanceof IPPacket) {
					IPPacket ipP = (IPPacket) p;
					EthernetPacket ethP = new EthernetPacket();

					ethP.frametype=EthernetPacket.ETHERTYPE_IP;
					ethP.src_mac=new byte[]{(byte)00,(byte)26,(byte)18,(byte)00,(byte)25,(byte)65};    
					ethP.dst_mac=new byte[]{(byte)90,(byte)230,(byte)186,(byte)60,(byte)205,(byte)90};
					ipP.datalink=ethP;

					System.out.println("Layer 2: Sending IP packet downwards");
					sendDownwards(ipP);
					//((EthernetPacket)p.datalink).frametype==EthernetPacket.ETHERTYPE_ARP
				}else if(p instanceof ARPPacket) {
					ARPPacket arpP = (ARPPacket) p;

					EthernetPacket ethP = new EthernetPacket();
					ethP.frametype = EthernetPacket.ETHERTYPE_ARP;
					ethP.src_mac = getMacAddr();

					switch (arpP.operation) {
					case ARPPacket.ARP_REQUEST:
						ethP.dst_mac = bcastAddr;
						break;

					case ARPPacket.ARP_REPLY:
						ethP.dst_mac = arpP.target_hardaddr;
						break;

					default:
						System.err.println("Layer 2: UNKNOWN PACKET");
						break;
					}

					arpP.datalink = ethP;

					System.out.println("Layer 2: Sending ARP packet downwards");
					sendDownwards(arpP);
				}else {
					System.err.println("ERROR: Layer2 line 74, else.");
				}
				
				

			} else {
				lowSemaphore.release();
			}

		}

		// while (!topLayer.hasFinished()) {
		// wait for the queues to be emptied
		// }
		// topLayer.close();
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
