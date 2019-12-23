package stack;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import util.Utils;

public class Layer3 extends Layer {
	private ProtocolARP protocolARP;
	private ProtocolICMP protocolICMP;

	private static final int IP_LENGTH = 4;
	private byte[] ipAddr = new byte[IP_LENGTH];
	private byte[] ipMask = new byte[IP_LENGTH];
	private byte[] ipGateway = new byte[IP_LENGTH];
	
	private Scanner scanner = new Scanner(System.in);

	public Layer3() {
		config();
		protocolARP.start();
		protocolICMP.start();
	}

	@Override
	public void config() {
		this.protocolARP = new ProtocolARP();
		this.protocolARP.setLowLayer(this);
		this.protocolICMP = new ProtocolICMP();
		this.protocolICMP.setLowLayer(this);

		this.ipAddr = requestIp();
		this.ipMask = requestMask();
		this.ipGateway = requestGateway();
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

				if (p != null && p.datalink != null) {
					EthernetPacket ethP = (EthernetPacket) p.datalink;
					int type = ethP.frametype;

					switch (type) {
					case EthernetPacket.ETHERTYPE_ARP:
						sendToProtocol(protocolARP, p);
						break;
					case EthernetPacket.ETHERTYPE_IP:
						sendToProtocol(protocolICMP, p);
						break;
					default:
						System.err.println("Layer3: Unsupported protocol detected, packet dropped.");
						break;
					}
				} else {
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
	
	private byte[] requestMask() {
		System.out.print("LAYER 3: Enter the network mask (XXX.XXX.XXX.XXX): ");
		String maskStr = scanner.nextLine();

		// validate the MAC address with a regular expression
		while (!isValidIp(maskStr)) {
			System.out.print("LAYER 3: Enter the network mask (XXX.XXX.XXX.XXX):");
			maskStr = scanner.nextLine();
		}

		// split into digits the String
		String[] maskArr = maskStr.split("\\.");

		// convert from String to byte[]
		byte[] mask = new byte[IP_LENGTH];

		for (int i = 0; i < IP_LENGTH; i++) {
			int digit = Integer.parseInt(maskArr[i]);
			mask[i] = (byte) digit;
		}
		//System.out.println(Utils.ipBytesToString(mask));
		return mask;
	}
	
	private byte[] requestGateway() {
		System.out.print("LAYER 3: Enter the default Gateway (XXX.XXX.XXX.XXX): ");
		String gatewayStr = scanner.nextLine();

		// validate the MAC address with a regular expression
		while (!isValidIp(gatewayStr)) {
			System.out.print("LAYER 3: Enter the default Gateway (XXX.XXX.XXX.XXX):");
			gatewayStr = scanner.nextLine();
		}

		// split into digits the String
		String[] gatewayArr = gatewayStr.split("\\.");

		// convert from String to byte[]
		byte[] gatewayAddr = new byte[IP_LENGTH];

		for (int i = 0; i < IP_LENGTH; i++) {
			int digit = Integer.parseInt(gatewayArr[i]);
			gatewayAddr[i] = (byte) digit;
		}

		return gatewayAddr;
	}

	public byte[] getIpAddr() {
		return ipAddr;
	}

	public byte[] getIpMask() {
		return ipMask;
	}

	public byte[] getIpGateway() {
		return ipGateway;
	}
	
	public ProtocolARP getProtocolARP() {
		return protocolARP;
	}

	public void setIpAddr(byte[] ipAddr) {
		this.ipAddr = ipAddr;
	}
	
	public Boolean isLocal(byte[] unknownIpAddr) {
		return Arrays.equals(Utils.bitwiseAnd(ipAddr, ipMask), Utils.bitwiseAnd(unknownIpAddr, ipMask));
	}
}
