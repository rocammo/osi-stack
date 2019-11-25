package stack;

public class ArpEntry {
	private static final int MAC_LENGTH = 6;
	private byte[] macAddr = new byte[MAC_LENGTH];
	long timestamp;
	
	ArpEntry(byte[] macAddr) {
		this.macAddr = macAddr;
		this.timestamp= System.currentTimeMillis();
	}

}
