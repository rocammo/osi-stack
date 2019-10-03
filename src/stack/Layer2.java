package stack;

import java.io.IOException;
import java.util.Scanner;

import jpcap.*;
import util.ReceivePackets;

public class Layer2 extends Layer {
	private String mac;

	private Scanner scanner = new Scanner(System.in);

	public Layer2(String mac) {
		this.mac = mac;
	}

	public Layer2() {
		config();
	}

	@Override
	public void config(Layer above, Layer below) {
		this.mac = selectMac();
		System.out.print("Layer 2 created with MAC: " + this.mac);

	}

	@Override
	public void run() {

		
		
	}

	private String selectMac() {
		System.out.print("Enter the desired MAC address: ");
		while (!scanner.hasNext()) {
			scanner.nextLine(); // clear the invalid input before prompting again
		}
		return scanner.nextLine();
	}


	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

}
