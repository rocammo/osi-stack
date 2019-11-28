package main;

import java.util.Arrays;

import stack.Layer1;
import stack.Layer2;
import stack.Layer3;

public class Main {
	public static void main(String[] args) {
		

		
		//System.exit(1);
		
		Layer1 physical = new Layer1();
		Layer2 datalink = new Layer2();
		Layer3 network = new Layer3();

		physical.setNeighbourLayers(datalink, null);
		datalink.setNeighbourLayers(network, physical);
		network.setNeighbourLayers(null, datalink);

		physical.start();
		datalink.start();
		network.start();

		try {
			Thread.sleep(150000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		physical.close();
	}
}
