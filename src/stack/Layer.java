package stack;

public abstract class Layer extends Thread {
	public Layer above, below;
	public abstract void config(Layer above, Layer below);
	public abstract void run();
}
