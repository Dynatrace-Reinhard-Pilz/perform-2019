package tokenizer;

import java.util.concurrent.atomic.AtomicBoolean;

public final class Stability {

	private static AtomicBoolean STABLE = new AtomicBoolean(true);
	
	public static void set(boolean isStable) {
		STABLE.set(isStable);
	}
	
	public static boolean get() {
		return STABLE.get();
	}

}
