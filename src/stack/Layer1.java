package stack;

import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import jpcap.*;
import jpcap.packet.Packet;

public class Layer1 extends Layer {
	private int interfaceId;

	private Scanner scanner = new Scanner(System.in);

	public Layer1(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	public Layer1() {
		config();
	}

	@Override
	public void config() {
		this.interfaceId = selectInterface();
	}

	@Override
	public void run() {
		NetworkInterface[] interfaces = JpcapCaptor.getDeviceList();

		try {
			System.out.print("\nOpening captor... ");
			JpcapCaptor captor = JpcapCaptor.openDevice(interfaces[interfaceId], 2000, false, 20);
			System.out.println("OK!");

			System.out.print("Opening sender... ");
			JpcapSender sender = JpcapSender.openDevice(interfaces[interfaceId]);
			System.out.println("OK!");

			while (running) {
				Packet packet = null;

				while (packet == null) {
					packet = captor.getPacket();

					if (packet != null) {
						// when a packet is received by the captor,
						// it is sent upwards (towards layer 2)
						sendUpwards(packet);
					}

					sendToNetwork(sender);
				}
			}

			// while (!topLayer.hasFinished()) {
			// wait for the queues to be emptied
			// }
			// topLayer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int selectInterface() {
		// obtain the list of network interfaces
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

		System.out.print("Layer 1: Select the interface you want to use: ");
		while (!scanner.hasNextInt()) {
			scanner.nextLine(); // clear the invalid input before prompting again
			System.out.print("Layer 1: Select the interface you want to use: ");
		}
		return scanner.nextInt();
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	private void sendToNetwork(JpcapSender sender) {
		try {
			topSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Iterator<Packet> itr = topQueue.iterator();

		while (itr.hasNext()) {
			Packet packet = topQueue.poll();
			sender.sendPacket(packet);

			System.out.println("Layer 1: Sending packet to the network. (Details below)");
			System.out.println(packet);
		}

		topSemaphore.release();
	}
}
