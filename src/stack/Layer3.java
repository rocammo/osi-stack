package stack;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

public class Layer3 extends Layer {
	private ProtocolARP protocolARP;
	private ProtocolIP protocolIP;
	
	private static final int IP_LENGTH = 4;
	private byte[] ipAddr = new byte[IP_LENGTH];
	
	private Scanner scanner = new Scanner(System.in);
	

	public Layer3() {
		config();
		protocolARP.start();
		protocolIP.start();
	}

	@Override
	public void config() {
		this.protocolARP = new ProtocolARP();
		this.protocolARP.setLowLayer(this);
		this.protocolIP = new ProtocolIP();
		this.protocolIP.setLowLayer(this);
		
		this.ipAddr = requestIp();
	}

	@Override
	public void run() {
		while (running) {
			try {
				lowSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (!lowQueue.isEmpty()) {
				Packet p = lowQueue.poll();
				lowSemaphore.release();
				
				if(p != null && p.datalink != null) {
					EthernetPacket ethP = (EthernetPacket) p.datalink;
					int type = ethP.frametype;

					switch (type) {
					case EthernetPacket.ETHERTYPE_ARP:
						sendToProtocol(protocolARP, p);
						break;
					case EthernetPacket.ETHERTYPE_IP:
						sendToProtocol(protocolIP, p);
						break;
					default:
						System.err.println("Layer3: Unsupported protocol detected, packet dropped.");
						break;
					}
				}else {
					System.err.println("L2: Error, p.datalink was null.");
					break;
				}
				
			} else {
				lowSemaphore.release();
			}
			
			if (!topQueue.isEmpty()) {
				Packet p = topQueue.poll();
				topSemaphore.release();

				System.out.println("Layer 3: Sending ARP packet downwards");
				sendDownwards(p);

			} else {
				lowSemaphore.release();	
			}
		}

		//while (!protocolARP.hasFinished() && !protocolIP.hasFinished()) {
			// wait for the queues to be emptied
		//}
		//protocolARP.close();
		//protocolIP.close();
	}

	public void sendToProtocol(Protocol protocol, Packet p) {
		try {
			protocol.semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		protocol.packets.add(p);

		protocol.semaphore.release();
	}
	
	private boolean isValidIp(String ipStr) {
		Pattern p = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
		Matcher m = p.matcher(ipStr);
		return m.find();
	}
	
	private byte[] requestIp() {
		System.out.print("LAYER 3: Enter the Layer3 IP address (XXX.XXX.XXX.XXX): ");
		String ipStr = scanner.nextLine();

		// validate the MAC address with a regular expression
		while (!isValidIp(ipStr)) {
			System.out.print("LAYER 3: Enter the Layer3 IP address (XXX.XXX.XXX.XXX):");
			ipStr = scanner.nextLine();
		}
		
		// split into digits the String
		String[] ipArr = ipStr.split("\\.");
		
		// convert from String to byte[]
		byte[] ipAddr = new byte[IP_LENGTH];

		for (int i = 0; i < IP_LENGTH; i++) {
			int digit = Integer.parseInt(ipArr[i]);
			ipAddr[i] = (byte) digit;
		}

		return ipAddr;
	}
	
	public byte[] getIpAddr() {
		return ipAddr;
	}
	
	public ProtocolARP getProtocolARP() {
		return protocolARP;
	}

	public void setIpAddr(byte[] ipAddr) {
		this.ipAddr = ipAddr;
	}
}

