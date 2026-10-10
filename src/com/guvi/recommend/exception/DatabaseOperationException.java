package com.guvi.recommend.exception;

public class DatabaseOperationException extends RecommendationException {
    public DatabaseOperationException(String message, Throwable cause) {
        super("ERR_DATABASE_OP", message, cause);
    }

    public DatabaseOperationException(String message) {
        super("ERR_DATABASE_OP", message);
    }
}
