package stack;

import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

public class Layer3 extends Layer {
	private ProtocolARP protocolARP;
	private ProtocolIP protocolIP;

	public Layer3() {
		config();
	}

	@Override
	public void config() {
		this.protocolARP = new ProtocolARP();
		this.protocolIP = new ProtocolIP();
	}

	@Override
	public void run() {
		while (true) {
			Packet p = null;
			EthernetPacket ethP = (EthernetPacket) p.datalink; // FIXME p.datalink
			int type = ethP.frametype;

			switch (type) {
			case EthernetPacket.ETHERTYPE_ARP:
				sendToProtocol(protocolARP, new Packet()); // FIXME new Packet()
				break;
			case EthernetPacket.ETHERTYPE_IP:
				sendToProtocol(protocolIP, new Packet()); // FIXME new Packet()
				break;
			default:
				break;
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
}
