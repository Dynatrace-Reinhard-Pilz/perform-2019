package tokenizer.util.http;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpHeaders {
	
	public static final String CONTENT_LENGTH = "Content-Length";
	
    private Map<String, List<String>> headers;

    public HttpHeaders() {
        headers = new LinkedHashMap<>();
    }
    
    public HttpHeaders add(String name, int value) {
    	return add(name, String.valueOf(value));
    }

    public HttpHeaders add(String name, String value) {
        name = name.toLowerCase();

        if (!headers.containsKey(name)) {
            headers.put(name, new ArrayList<>());
        }
        headers.get(name).add(value);
        return this;
    }

    public Set<String> keySet() {
        return headers.keySet();
    }

    public List<String> get(String key) {
        return headers.get(key);
    }

    @Override
    public String toString() {
        return "HttpHeaders{" +
                "headers=" + headers +
                '}';
    }
}
