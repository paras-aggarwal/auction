package org.deutschebank.auction.biding.exception;

import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {

    private String code;
    private String[] messages;

    public InvalidRequestException(String code, String... messages) {
        super(messages != null && messages.length > 0 ? messages[0] : null);
        setParams(code, messages);
    }

    public InvalidRequestException(String code, Throwable cause, String... messages) {
        super(messages != null && messages.length > 0 ? messages[0] : null, cause);
        setParams(code, messages);
    }

    private void setParams(String code, String... messages) {
        this.code = code;
        this.messages = messages;
    }

}
