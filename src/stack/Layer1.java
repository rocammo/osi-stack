package stack;

import java.io.IOException;
import java.util.Scanner;

import jpcap.*;
import util.ReceivePackets;

public class Layer1 extends Layer {
	private int interfaceId;
	private int bufferSize;

	private Scanner scanner = new Scanner(System.in);

	public Layer1(int interfaceId, int bufferSize) {
		this.interfaceId = interfaceId;
		this.bufferSize = bufferSize;
	}

	public Layer1() {
		config();
	}

	@Override
	public void config(Layer above, Layer below) {
		this.interfaceId = selectInterface();
		this.bufferSize = resizeBuffer();
	}

	@Override
	public void run() {
		NetworkInterface[] interfaces = JpcapCaptor.getDeviceList();

		try {
			// initializes and returns interface
			JpcapCaptor jpcap = JpcapCaptor.openDevice(interfaces[interfaceId], 2000, false, 20);
			// for each pack creates a ReceivePackets object and ends in the receivePacket()
			// method
			jpcap.loopPacket(bufferSize, new ReceivePackets(interfaces[selectInterface()]));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int selectInterface() {
		// Obtain the list of network interfaces
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();

		// for each network interface
		for (int i = 0; i < devices.length; i++) {
			// print out its name and description
			System.out.println(i + ": " + devices[i].name + "(" + devices[i].description + ")");

			// print out its datalink name and description
			System.out.println(" datalink: " + devices[i].datalink_name + "(" + devices[i].datalink_description + ")");

			// print out its MAC address
			System.out.print(" MAC address:");
			for (byte b : devices[i].mac_address)
				System.out.print(Integer.toHexString(b & 0xff) + ":");
			System.out.println();

			// print out its IP address, subnet mask and broadcast address
			for (NetworkInterfaceAddress a : devices[i].addresses)
				System.out.println(" address:" + a.address + " " + a.subnet + " " + a.broadcast);
		}
		System.out.println();

		System.out.print("Select the interface you want to use: ");
		while (!scanner.hasNextInt()) {
			scanner.nextLine(); // clear the invalid input before prompting again
			System.out.print("Select the interface you want to use: ");
		}
		return scanner.nextInt();
	}

	private int resizeBuffer() {
		System.out.print("What buffer size do you need? ");
		while (!scanner.hasNextInt()) {
			scanner.nextLine(); // clear the invalid input before prompting again
			System.out.print("What buffer size do you need? ");
		}
		return scanner.nextInt();
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
