package com.guvi.recommend.exception;

public class InvalidParameterException extends RecommendationException {
    public InvalidParameterException(String paramName, String message) {
        super("ERR_INVALID_PARAM", "Invalid parameter [" + paramName + "]: " + message);
    }
}
