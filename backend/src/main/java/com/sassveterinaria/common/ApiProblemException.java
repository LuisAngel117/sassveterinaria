package com.sassveterinaria.common;

import org.springframework.http.HttpStatus;

public class ApiProblemException extends RuntimeException {

    private final HttpStatus status;
    private final String type;
    private final String title;
    private final String errorCode;

    public ApiProblemException(HttpStatus status, String type, String title, String detail, String errorCode) {
        super(detail);
        this.status = status;
        this.type = type;
        this.title = title;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
