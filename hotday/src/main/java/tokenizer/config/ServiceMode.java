package tokenizer.config;

public enum ServiceMode {

	Frontend, Backend;
	
	public static ServiceMode resolve(String value) {
		if (value == null) {
			return ServiceMode.Frontend;
		}
		if (value.toUpperCase().equals("BACKEND")) {
			return ServiceMode.Backend;
		}
		return ServiceMode.Frontend;
	}
}
