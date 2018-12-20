package tokenizer;

import java.util.concurrent.atomic.AtomicBoolean;

public final class Stability {
	
	public static final String HTTP_HEADER = "X-hot-day-stable";

	private static AtomicBoolean STABLE = new AtomicBoolean(true);
	
	public static void set(boolean isStable) {
		STABLE.set(isStable);
	}
	
	public static boolean get() {
		return STABLE.get();
	}

}
