package stack;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import util.Utils;

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
			
			if (!lowQueue.isEmpty()) {
				Packet p = lowQueue.poll();
				lowSemaphore.release();

				if (p != null && p.datalink != null) {
					EthernetPacket ep = (EthernetPacket) p.datalink;

					// Collect the packets destinated to us or to broadcast
					if (Arrays.equals(ep.dst_mac, macAddr) || Arrays.equals(ep.dst_mac, bcastAddr)) {
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
					
					Layer2 datalink = this;
					
					Thread thread = new Thread(new Runnable()
					{
					   public void run()
					   {
						   
						   EthernetPacket ethP = new EthernetPacket();
							
							byte[] ipAddr = ipP.dst_ip.getAddress();
						
							byte[] destinationHardwarAdd;
							
							Layer3 network = (Layer3)datalink.getTopLayer();
							
							if( network.isLocal(ipAddr) ) {
								System.out.println("sendICMP: Destination ip ("+ Utils.ipBytesToString(ipAddr) +") is local to our network.");
								destinationHardwarAdd = ProtocolARP.resolveIP(network.getProtocolARP(), ipAddr);
								
							}else {
								System.out.println("sendICMP: Destination ip ("+ Utils.ipBytesToString(ipAddr) +") is outside from our network.");
								destinationHardwarAdd = ProtocolARP.resolveIP(network.getProtocolARP(), network.getIpGateway());
							}
							
							if(destinationHardwarAdd == null) {
								System.err.println("sendICMP: IP is not online, no ARP response given." );
							}else {
								//System.out.println( Utils.macBytesToString(destinationHardwarAdd) );
								ethP.frametype=EthernetPacket.ETHERTYPE_IP;
								ethP.src_mac = datalink.getMacAddr();
								ethP.dst_mac = destinationHardwarAdd;
								ipP.datalink = ethP;

								System.out.println("Layer 2: Sending IP packet downwards");
								datalink.sendDownwards(ipP);
							}
					   }
					});

					thread.start();
				
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
					System.err.println("ERROR: Layer2 incorrect packet type.");
				}

			} else {
				lowSemaphore.release();
			}

		}

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
