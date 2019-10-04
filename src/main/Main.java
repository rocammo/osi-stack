package main;

import stack.Layer1;
import stack.Layer2;

public class Main {
	public static void main(String[] args) {
		Layer1 physical = new Layer1();
		Layer2 datalink = new Layer2();
		
		physical.setTopLayer(datalink);
		datalink.setLowLayer(physical);
		
		physical.run();
		datalink.run();
	}
}
