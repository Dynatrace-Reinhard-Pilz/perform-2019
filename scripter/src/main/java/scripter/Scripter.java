package scripter;

import javax.script.*;

@SuppressWarnings("restriction")
public final class Scripter {

	public static void main(String[] args) throws Exception {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
		Bindings obj = (Bindings)engine.eval("var obj = { value: 1 };  obj; ");
		Integer value = (Integer)obj.get("value");
		System.out.println(value);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(Bindings obj) {
		return (T) obj.get("value");
	}

}
