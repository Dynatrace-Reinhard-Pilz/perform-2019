package tokenizer.util;

public final class ShutdownThread extends Thread {

	public ShutdownThread() {
		super(ShutdownThread.class.getSimpleName());
		setDaemon(true);
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		System.exit(0);
	}
}
