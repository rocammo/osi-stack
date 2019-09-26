package util;

import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;

import java.io.IOException;


public class ReceivePackets implements PacketReceiver{
	private NetworkInterface device;
	
	public ReceivePackets(NetworkInterface device) {
		this.device = device;
	}
	
	public void receivePacket(Packet packet) {
		System.out.println(packet);
		try {
			JpcapSender sender=JpcapSender.openDevice(device);
			sender.sendPacket(packet);
			System.out.println("Packet sent!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
