package org.deutschebank.auction.biding.exception.exceptionhandler;

import lombok.extern.log4j.Log4j2;
import org.deutschebank.auction.biding.exception.BusinessException;
import org.deutschebank.auction.biding.exception.InvalidRequestException;
import org.deutschebank.auction.biding.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@Log4j2
@ControllerAdvice
@RestController
public class HttpServerExceptionHandler {

    private static final String UNAUTH01 = "UNAUTH01";
    private static final String GENEX01 = "GENEX01";

    private ApiError getCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        if (throwable instanceof InvalidRequestException) {
            return handleInvalidRequestException((InvalidRequestException) throwable);
        } else if (throwable instanceof BusinessException) {
            return handleBusinessException((BusinessException) throwable);
        } else if (throwable instanceof AccessDeniedException) {
            return handleAccessDeniedException((AccessDeniedException) throwable);
        } else {
            return handleUnHandledException(throwable);
        }
    }

    private String[] convertToMessagesArray(String message) {
        String[] messages = new String[0];
        if (message != null) {
            messages = new String[1];
            messages[0] = message;
        }
        return messages;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @org.springframework.web.bind.annotation.ExceptionHandler(value = InvalidRequestException.class)
    public ApiError handleInvalidRequestException(InvalidRequestException invalidRequestException) {
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(invalidRequestException.getCode())
                .messages(invalidRequestException.getMessages())
                .exception(invalidRequestException.getClass().getName())
                .cause(getCause(invalidRequestException.getCause()))
                .build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler(value = BusinessException.class)
    public ApiError handleBusinessException(BusinessException businessException) {
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(businessException.getCode())
                .messages(businessException.getMessages())
                .exception(businessException.getClass().getName())
                .cause(getCause(businessException.getCause()))
                .build();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @org.springframework.web.bind.annotation.ExceptionHandler(value = ResourceNotFoundException.class)
    public ApiError handleResourceNotFoundException(ResourceNotFoundException resourceNotFoundException) {
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .code(resourceNotFoundException.getCode())
                .messages(resourceNotFoundException.getMessages())
                .exception(resourceNotFoundException.getClass().getName())
                .cause(getCause(resourceNotFoundException.getCause()))
                .build();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @org.springframework.web.bind.annotation.ExceptionHandler(value = AccessDeniedException.class)
    public ApiError handleAccessDeniedException(AccessDeniedException accessDeniedException) {
        return ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .code(UNAUTH01)
                .messages(convertToMessagesArray(accessDeniedException.getMessage()))
                .exception(accessDeniedException.getClass().getName())
                .cause(getCause(accessDeniedException.getCause()))
                .build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler(value = Throwable.class)
    public ApiError handleUnHandledException(Throwable throwable) {
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(GENEX01)
                .messages(convertToMessagesArray(throwable.getMessage()))
                .exception(throwable.getClass().getName())
                .cause(getCause(throwable.getCause()))
                .build();
    }
}
