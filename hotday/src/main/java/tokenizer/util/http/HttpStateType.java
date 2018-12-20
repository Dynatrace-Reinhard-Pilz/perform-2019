package tokenizer.util.http;

enum HttpStateType {
    NOT_CONNECTED,
    CONNECTED,
    HEADER_SENT,
    ENTITY_SENT,
    HEADER_RECEIVED;
}
