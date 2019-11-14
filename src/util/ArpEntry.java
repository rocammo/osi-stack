package util;

public class ArpEntry {
	private static final int MAC_LENGTH = 6;
	//private static final int IP_LENGTH = 4;
	private byte[] macAddr = new byte[MAC_LENGTH];
	//private byte[] ipAddr = new byte[IP_LENGTH];
	long timestamp;
	
	ArpEntry(byte[] macAddr) {
		this.macAddr = macAddr;
		//this.ipAddr = ipAddr;
		this.timestamp= System.currentTimeMillis();
	}

}
