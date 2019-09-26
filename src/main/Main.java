package main;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import stack.Layer1;

@SuppressWarnings("unused")
public class Main {
	public static void main(String[] args) {
		Layer1 wlan = new Layer1();
		wlan.run();
	}
}
