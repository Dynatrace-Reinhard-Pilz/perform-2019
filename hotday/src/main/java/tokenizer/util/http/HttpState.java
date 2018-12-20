package tokenizer.util.http;

import java.nio.ByteBuffer;

class HttpState {
	
	ByteBuffer buf;
	HttpHandler httpHandler;
	HttpStateType state;
	HttpRequest httpRequest;
	long read;

    public HttpState(HttpRequest httpRequest, HttpHandler httpHandler) {
        this.state = HttpStateType.NOT_CONNECTED;
        this.httpRequest = httpRequest;
        this.httpHandler = httpHandler;
        this.buf = ByteBuffer.allocate(1024);
        this.read = 0;
    }
}
