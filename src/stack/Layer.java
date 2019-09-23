package stack;

public abstract class Layer extends Thread {
	public abstract void config();
	public abstract void run();
}
