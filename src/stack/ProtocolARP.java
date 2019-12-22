package stack;

import jpcap.packet.ARPPacket;
import jpcap.packet.Packet;
import util.Utils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ProtocolARP extends Protocol {

	// IP & ArpEntry ( MAC and timestamp )
	HashMap<String, ArpEntry> arpTable = new HashMap<String, ArpEntry>();

	public static void arpQueryApp(ProtocolARP protocolARP) {

		/** while (true) {
			try {
				TimeUnit.SECONDS.sleep(3); // Wait between Queries
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Scanner scanner = new Scanner(System.in);

			System.out.println("ARP Query App: Enter the IP to resolve (XXX.XXX.XXX.XXX): ");
			String ipStr = scanner.nextLine();

			// split into digits the String
			String[] ipArr = ipStr.split("\\.");

			// convert from String to byte[]
			byte[] ipAddr = new byte[4];

			for (int i = 0; i < 4; i++) {
				int digit = Integer.parseInt(ipArr[i]);
				ipAddr[i] = (byte) digit;
			}

			Boolean answerOnCache = false;

			if (protocolARP.arpTable.containsKey(Utils.ipBytesToString(ipAddr))) {
				ArpEntry answer = protocolARP.arpTable.get(Utils.ipBytesToString(ipAddr));

				// Checking its freshness
				if ((System.currentTimeMillis() - 30 * 1000) < answer.getTimestamp()) {
					// If existing record has not expired (30 seconds) serve it.
					answerOnCache = true;
					System.out
							.println("ARP Query App: Answer was already on our table, " + Utils.ipBytesToString(ipAddr)
									+ " belongs to " + Utils.macBytesToString(answer.getMacAddr()));

				} else {
					System.out.println("ARP Query App: Answer was on our table but had expired.");
				}

			}
			if (!answerOnCache) { // If we dont have a fresh answer on cache, send ARP request
				Layer3 network = (Layer3) protocolARP.getLowLayer();
				Layer2 datalink = (Layer2) network.getLowLayer();

				byte[] senderHardwarAdd = datalink.getMacAddr();
				byte[] senderProtoAdd = network.getIpAddr();
				byte[] target_protoaddr = ipAddr;

				ARPPacket arpRequest = ProtocolARP.generateArpRequest(senderHardwarAdd, senderProtoAdd,
						target_protoaddr);
				System.out.println("Layer 3: Sending ARP packet downwards");
				network.sendDownwards(arpRequest);

				long start = System.currentTimeMillis();
				long end = start + 3 * 1000; // 3 seconds
				while (true) {

					if (protocolARP.arpTable.containsKey(Utils.ipBytesToString(target_protoaddr))) {
						ArpEntry answer = protocolARP.arpTable.get(Utils.ipBytesToString(target_protoaddr));
						System.out.println("ARP Query App: Answer received, " + Utils.ipBytesToString(target_protoaddr)
								+ " belongs to " + Utils.macBytesToString(answer.getMacAddr()));
						break;
					}

					if (System.currentTimeMillis() > end) {
						System.err.println("ARP Query App: timeout for " + Utils.ipBytesToString(target_protoaddr));
						break;
					}
				}
			}

		}**/

	}
	
	public static byte[] resolveIP(ProtocolARP protocolARP, byte[] ipAddr) {

			Boolean answerOnCache = false;

			if (protocolARP.arpTable.containsKey(Utils.ipBytesToString(ipAddr))) {
				ArpEntry answer = protocolARP.arpTable.get(Utils.ipBytesToString(ipAddr));

				// Checking its freshness
				if ((System.currentTimeMillis() - 30 * 1000) < answer.getTimestamp()) {
					// If existing record has not expired (30 seconds) serve it.
					answerOnCache = true;
					System.out
							.println("DEBUG resolveIP: Answer was already on our table, " + Utils.ipBytesToString(ipAddr)
									+ " belongs to " + Utils.macBytesToString(answer.getMacAddr()));

				} else {
					System.out.println("DEBUG resolveIP: Answer was on our table but had expired.");
				}

			}
			if (!answerOnCache) { // If we dont have a fresh answer on cache, send ARP request
				Layer3 network = (Layer3) protocolARP.getLowLayer();
				Layer2 datalink = (Layer2) network.getLowLayer();

				byte[] senderHardwarAdd = datalink.getMacAddr();
				byte[] senderProtoAdd = network.getIpAddr();
				byte[] target_protoaddr = ipAddr;

				ARPPacket arpRequest = ProtocolARP.generateArpRequest(senderHardwarAdd, senderProtoAdd,
						target_protoaddr);
				System.out.println("Layer 3: Sending ARP packet downwards");
				network.sendDownwards(arpRequest);

				long start = System.currentTimeMillis();
				long end = start + 6 * 1000; // 6 seconds
				while (true) {
					
					if (protocolARP.arpTable.containsKey(Utils.ipBytesToString(target_protoaddr))) {
						ArpEntry answer = protocolARP.arpTable.get(Utils.ipBytesToString(target_protoaddr));
						System.out.println("DEBUG resolveIP: Answer received, " + Utils.ipBytesToString(target_protoaddr)
								+ " belongs to " + Utils.macBytesToString(answer.getMacAddr()));
						return answer.getMacAddr();
					}

					if (System.currentTimeMillis() > end) {
						System.err.println("DEBUG resolveIP: timeout for " + Utils.ipBytesToString(target_protoaddr));
						return null;
					}
				}
			}
			return null;



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

	public static ARPPacket generateArpResponse(byte[] senderHardwarAdd, byte[] senderProtoAdd, byte[] target_hardaddr,
			byte[] target_protoaddr) {
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
		arp.target_protoaddr = target_protoaddr;

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

				switch (arpPacket.operation) {
				case ARPPacket.ARP_REQUEST:
					System.out.println("ProtocolARP: ARP REQUEST received. ");

					if (Arrays.equals(arpPacket.target_protoaddr, (((Layer3) getLowLayer()).getIpAddr()))) {
						// Arp requests directed to our IP -> Reply with our MAC
						ARPPacket arpResponse = generateArpResponse(arpPacket.sender_hardaddr,
								((Layer3) getLowLayer()).getIpAddr(),
								((Layer2) getLowLayer().getLowLayer()).getMacAddr(), arpPacket.sender_protoaddr);
						System.out.println("ProtocolARP: Sending ARP response.");
						getLowLayer().sendDownwards(arpResponse);

					} else {
						// Else we drop the packet, as we cannot answer it
						System.out.println("ProtocolARP: Dropping the packet as is not directed to resolve our IP"
								+ ", it was asking for " + Utils.ipBytesToString(arpPacket.target_protoaddr));
					}

					break;

				case ARPPacket.ARP_REPLY:
					System.out.println("ProtocolARP: ARP REPLY received. ");

					System.out.println(
							"ProtocolARP: Adding to ARPTable " + Utils.ipBytesToString(arpPacket.sender_protoaddr) + " "
									+ Utils.macBytesToString(arpPacket.sender_hardaddr));

					arpTable.put(Utils.ipBytesToString(arpPacket.sender_protoaddr),
							new ArpEntry(arpPacket.sender_hardaddr));

					break;

				default:
					System.err.println("ProtocolARP: UNKNOWN packet received.");
					break;
				}

			} else {
				semaphore.release();
			}
		}
	}
}