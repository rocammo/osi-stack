package stack;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		while (true) {
			try {
				lowSemaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (lowQueue.isEmpty()) {
				lowSemaphore.release();
				continue;
			}

			Packet p = lowQueue.remove();
			lowSemaphore.release();

			// if the package comes from another hosts, then, modify the packet
			// by setting the destination MAC address as broadcast, so that the
			// packet can be sent to all devices on the network
			EthernetPacket ep = (EthernetPacket) p.datalink;

			if (!Arrays.equals(ep.dst_mac, macAddr) && !Arrays.equals(ep.dst_mac, bcastAddr)) {
				sendUpwards(p);
			}

		}
	}

	private byte[] requestMac() {
		System.out.print("Enter the source MAC address (XX:XX:XX:XX:XX:XX): ");
		String macStr = scanner.nextLine();

		// validate the MAC address with a regular expression
		while (!isValidMac(macStr)) {
			System.out.print("Enter the source MAC address (XX:XX:XX:XX:XX:XX): ");
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
