package stack;

import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

public class Layer3 extends Layer {
	private ProtocolARP protocolARP;
	private ProtocolIP protocolIP;

	public Layer3() {
		config();
		protocolARP.start();
		protocolIP.start();
	}

	@Override
	public void config() {
		this.protocolARP = new ProtocolARP();
		this.protocolIP = new ProtocolIP();
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

				EthernetPacket ethP = (EthernetPacket) p.datalink;
				int type = ethP.frametype;

				switch (type) {
				case EthernetPacket.ETHERTYPE_ARP:
					sendToProtocol(protocolARP, p);
					break;
				case EthernetPacket.ETHERTYPE_IP:
					sendToProtocol(protocolIP, p);
					break;
				default:
					System.out.println("Layer3: Unsupported protocol detected, packet dropped.");
					break;
				}
			} else {
				lowSemaphore.release();
			}
		}

		while (!protocolARP.hasFinished() && !protocolIP.hasFinished()) {
			// wait for the queues to be emptied
		}
		protocolARP.close();
		protocolIP.close();
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
