package com.zerobase.zerobasestudy.util.exception;

public class SqlException extends RuntimeException{

    public SqlException(String message, Throwable cause) {
        super(message, cause);
    }
}