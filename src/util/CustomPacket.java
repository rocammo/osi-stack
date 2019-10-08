/* DISCLAIMER
 * --
 * 
 * This class will be useful in case of considering a single queue per layer and 
 * that the packet is the one that contains the packet direction.
 */

package util;

import jpcap.packet.Packet;

public class CustomPacket {
	public enum PacketDirection {
		UPWARDS, DOWNWARDS
	}

	private int packetId;
	private Packet packet;
	private PacketDirection packetDirection;

	public CustomPacket(int packetId, Packet packet, PacketDirection packetDirection) {
		this.packetId = packetId;
		this.packet = packet;
		this.packetDirection = packetDirection;
	}

	public int getPacketId() {
		return packetId;
	}

	public void setPacketId(int packetId) {
		this.packetId = packetId;
	}

	public Packet getPacket() {
		return packet;
	}

	public void setPacket(Packet packet) {
		this.packet = packet;
	}

	public PacketDirection getPacketDirection() {
		return packetDirection;
	}

	public void setPacketDirection(PacketDirection packetDirection) {
		this.packetDirection = packetDirection;
	}
}
