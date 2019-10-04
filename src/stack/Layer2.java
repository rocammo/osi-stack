package stack;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		this.macAddr = requestMAC();
	}

	@Override
	public void run() {
		
	}
	
	private byte[] requestMAC() {
		System.out.print("Enter the source MAC address (XX:XX:XX:XX:XX:XX): ");
		String macStr = scanner.nextLine();
		
		// validate the MAC address with a regular expression
		while (!isValidMAC(macStr)) {
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
	
	private boolean isValidMAC(String macStr) {
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
