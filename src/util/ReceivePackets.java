package util;

import jpcap.PacketReceiver;
import jpcap.packet.Packet;

public class ReceivePackets implements PacketReceiver {
	@Override
	public void receivePacket(Packet packet) {
		System.out.println(packet);
	}
}
