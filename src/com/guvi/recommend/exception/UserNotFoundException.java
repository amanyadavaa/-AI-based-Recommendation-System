package com.guvi.recommend.exception;

public class UserNotFoundException extends RecommendationException {
    public UserNotFoundException(String userId) {
        super("ERR_USER_NOT_FOUND", "User profile not found with ID: " + userId);
    }
}
