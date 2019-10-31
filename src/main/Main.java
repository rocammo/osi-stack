package main;

import stack.Layer1;
import stack.Layer2;
import stack.Layer3;

public class Main {
	public static void main(String[] args) {
		Layer1 physical = new Layer1();
		Layer2 datalink = new Layer2();
		Layer3 network = new Layer3();

		physical.setNeighbourLayers(datalink, null);
		datalink.setNeighbourLayers(network, physical);
		network.setNeighbourLayers(null, datalink);

		// TODO STOP WHEN BUFFER ENDS

		physical.start();
		datalink.start();
		network.start();
	}
}
