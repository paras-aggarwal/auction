package org.deutschebank.auction.users.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private String code;
    private String[] messages;

    public BusinessException(String code, String... messages) {
        super(messages != null && messages.length > 0 ? messages[0] : null);
        setParams(code, messages);
    }

    public BusinessException(String code, Throwable cause, String... messages) {
        super(messages != null && messages.length > 0 ? messages[0] : null, cause);
        setParams(code, messages);
    }

    private void setParams(String code, String... messages) {
        this.code = code;
        this.messages = messages;
    }

}
