package org.deutschebank.auction.users.exception.exceptionhandler;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.Date;

@Value
@Builder
public class ApiError implements Serializable {

    private int status;
    private String code;
    private String[] messages;
    private String exception;
    private ApiError cause;
    private Date timestamp = new Date();

}
