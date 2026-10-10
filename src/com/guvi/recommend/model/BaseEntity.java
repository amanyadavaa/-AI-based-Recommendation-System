package com.guvi.recommend.model;

/**
 * Abstract BaseEntity demonstrating Inheritance across domain models.
 */
public abstract class BaseEntity implements Identifiable {
    protected String id;
    protected long createdAt;
    protected long updatedAt;

    public BaseEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public BaseEntity(String id) {
        this.id = id;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
