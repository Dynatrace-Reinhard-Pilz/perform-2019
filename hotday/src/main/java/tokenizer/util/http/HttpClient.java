package tokenizer.util.http;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class HttpClient implements Closeable {
    private static final Logger log = Logger.getLogger(HttpClient.class.getName());
    private Set<SocketChannel> channels;

    private final Selector selector;

    public HttpClient() throws IOException {
        selector = Selector.open();
        this.channels = new HashSet<>();
    }

    public void get(URI uri, HttpHandler httpHandler) throws IOException {
        log.info("get: " + uri);
        this.request(HttpRequest.get(uri), httpHandler);
    }

    public void request(HttpRequest request, HttpHandler handler) throws IOException {
        // TODO keep-alive support
        request.getHeaders().add("Connection", "close");

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(request.getHost(), request.getPort()));

        SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT);
        selectionKey.attach(new HttpState(request, handler));
        this.channels.add(socketChannel);
    }

    public void waitAll() {
        while (!this.channels.isEmpty()) {
            try {
                selector.select();
            } catch (IOException e) {
                log.warning(e.getMessage());
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            if (!keys.isEmpty()) {
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();

                    HttpState state = (HttpState) key.attachment();

                    if (key.isConnectable()) {
                        log.info("connecting connection!");
                        SocketChannel sch = (SocketChannel) key.channel();
                        try {
                            sch.finishConnect();
                            state.state = HttpStateType.CONNECTED;
                            log.info(String.valueOf(sch.isBlocking()));
                            log.info(String.valueOf(sch.isConnected()));
                            key.interestOps(SelectionKey.OP_WRITE);
                        } catch (IOException e) {
                            try {
                                sch.close();
                            } catch (IOException e1) {
                                log.info("Can't close connection: " + e1.getMessage());
                            }
                        }
                    } else if (key.isWritable()) {
                        log.info("writing");
                        SocketChannel sch = (SocketChannel) key.channel();
                        try {
                            if (state.state == HttpStateType.CONNECTED) {
                                ByteBuffer src = ByteBuffer.wrap(
                                        state.httpRequest.getHeaderPartAsString().getBytes(StandardCharsets.US_ASCII));
                                sch.write(src);
                                state.state = HttpStateType.HEADER_SENT;
                            } else {
                                // TODO send entity
                                if (!state.httpRequest.writeEntity(sch)) {
                                    state.state = HttpStateType.ENTITY_SENT;
                                    key.interestOps(SelectionKey.OP_READ);
                                }
                            }
                        } catch (IOException e) {
                            try {
                                sch.close();
                            } catch (IOException e1) {
                                log.info("Can't close connection: " + e1.getMessage());
                            }
                        }
                    } else if (key.isReadable()) {
                        log.info("reading");
                        SocketChannel sch = (SocketChannel) key.channel();
                        try {
                            ByteBuffer buf = state.buf;
                            int read = sch.read(buf);
                            if (read <= 0) {
                                sch.close();
                                this.channels.remove(sch);
                                continue;
                            }
                            log.info("got " + read);
                            buf.flip();
                            if (state.state == HttpStateType.ENTITY_SENT) {
                                String got = new String(buf.array(), buf.arrayOffset() + buf.position(),
                                        buf.remaining(), StandardCharsets.UTF_8);
                                HttpResponse.HttpResponseParserResult result = HttpResponse.parse(got);
                                switch (result.getCode()) {
                                    case OK:
                                        log.info("PARSED");
                                        state.state = HttpStateType.HEADER_RECEIVED;
                                        state.httpHandler.onHeader(result.getResponse());
                                        state.read += result.getRemains().remaining();
                                        state.httpHandler.onBody(result.getRemains());
                                        break;
                                    case PARTIAL:
                                        log.info("PARTIAL");
                                        break;
                                }
                            } else {
                                state.read += buf.remaining();
                                state.httpHandler.onBody(buf);
                            }
                        } catch (IOException e) {
                            log.info("Can't read from connection: " + e.getMessage());
                        }
                    }
                    if (!key.isValid()) {
                        try {
                            key.channel().close();
                        } catch (IOException e) {
                            log.info("Can't close connection: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        selector.close();
    }

    public void post(URI uri, String content, HttpHandler httpHandler) throws IOException {
        StringHttpRequest httpRequest = new StringHttpRequest("POST", uri, content);
        this.request(httpRequest, httpHandler);
    }
}