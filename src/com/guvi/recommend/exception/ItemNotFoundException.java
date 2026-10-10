package com.guvi.recommend.exception;

public class ItemNotFoundException extends RecommendationException {
    public ItemNotFoundException(String itemId) {
        super("ERR_ITEM_NOT_FOUND", "Catalog item not found with ID: " + itemId);
    }
}
