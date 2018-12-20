package tokenizer.util.http;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Logger;

class StringHttpRequest extends HttpRequest {
	
	private static final Logger log = Logger.getLogger(HttpClient.class.getName());

    private final ByteBuffer buffer;
    private final byte[] bytes;
    private long bytesWritten = 0;

    public StringHttpRequest(String method, URI uri, String content) {
        super(method, uri);
        content = Objects.requireNonNull(content);
        getHeaders().add(HttpHeaders.CONTENT_LENGTH, content.length());
        this.bytes = content.getBytes(StandardCharsets.UTF_8);
        this.buffer = ByteBuffer.wrap(this.bytes);
    }

    @Override
    public boolean writeEntity(SocketChannel sch) throws IOException {
        int wrote = sch.write(buffer);
        bytesWritten += wrote;
        log.info("written " + wrote);
        return this.bytes.length != bytesWritten;
    }
    
}
