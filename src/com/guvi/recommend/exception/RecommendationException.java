package com.guvi.recommend.exception;

/**
 * Base checked exception for the recommendation system.
 */
public class RecommendationException extends Exception {
    private final String errorCode;

    public RecommendationException(String message) {
        super(message);
        this.errorCode = "ERR_RECOMMEND_GENERIC";
    }

    public RecommendationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RecommendationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
